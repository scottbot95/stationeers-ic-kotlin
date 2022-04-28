package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.util.toInt
import kotlin.jvm.JvmInline

sealed interface AccessSource {
    object Undefined : AccessSource

    @JvmInline
    value class Statement(val statement: IRStatement) : AccessSource

    data class Global(val index: Int) : AccessSource
    data class Parameter(val functionName: String, val paramIndex: Int) : AccessSource
}

typealias AccessInfo = MutableMap<IRRegister, RegisterAccessInfo>

data class RegisterAccessInfo(
    val reads: MutableSet<AccessSource> = mutableSetOf(),
    var writer: AccessSource? = null,
)

/**
 * For each statement, build a map of where potential value sources for each register.
 *
 * The test register of a [IRStatement.ConditionalStatement] is read-only,
 * however based of the path took we can infer its value.
 * This inference should be treated as a write-source for some optimizations.
 *
 * @param followCopies Track sources of values in register across [IRStatement.Copy] statements
 * @param includeBranchAsWriter [IRStatement.ConditionalStatement]s are treated as a write-source for the test register.
 */
fun IRCompilation.generateAccessInfo(
    followCopies: Boolean,
    includeBranchAsWriter: Boolean = false
): Map<IRStatement, AccessInfo> {

    val data = mutableMapOf<IRStatement, AccessInfo>()

    allEntrypoints.forEach { (name, entrypoint) ->
        val state = initState(name, entrypoint)

        trace(data, entrypoint, state, followCopies, includeBranchAsWriter)
    }

    return data
}

private fun trace(
    data: MutableMap<IRStatement, AccessInfo>,
    statement: IRStatement,
    state: MutableMap<IRRegister, AccessSource>,
    followCopies: Boolean,
    includeBranchAsWriter: Boolean
) {
    val myData = data.getOrPut(statement) { mutableMapOf() }
    var changes = 0
    changes += state.entries.sumOf { (reg, source) ->
        val info = myData.getOrPut(reg) { RegisterAccessInfo() }
        val added = info.writer != source
        info.writer = source
        added.toInt()
    }

    if (followCopies && statement is IRStatement.Copy) {
        if (changes == 0) return

        // After this statement, sources of dest are the same as sources of src
        state[statement.dest] = state[statement.src]!!
    } else {
        val self = AccessSource.Statement(statement)
        statement.readParams().forEach { reg ->
            val writer = (state[reg] as? AccessSource.Statement)?.statement as? IRStatement.WritingStatement
            val writeDest = writer?.dest

            // add this statement as read source to writers access info
            if ((writeDest == reg)) {
                changes += data.getOrPut(writer) { mutableMapOf() }
                    .getOrPut(reg) { RegisterAccessInfo() }
                    .reads
                    .add(self)
                    .toInt()
            }
        }

        if (changes == 0) return

        if (statement is IRStatement.WritingStatement) {
            // At this point, we are the only write source for any registers we write to
            state[statement.dest] = self
            myData[statement.dest]!!.writer = self
        } else if (includeBranchAsWriter && statement is IRStatement.ConditionalStatement) {
            // Optionally count branches as writes since we can infer value
            state[statement.check] = self
            myData[statement.check]!!.writer = self
        }
    }

    (statement as? IRStatement.ConditionalStatement)?.cond?.let { cond ->
        val newState = if (statement.next == null) {
            state
        } else {
            state.toMutableMap()
        }
        trace(data, cond, newState, followCopies, includeBranchAsWriter)
    }

    statement.next?.let { next ->
        trace(data, next, state, followCopies, includeBranchAsWriter)
    }
}

private fun IRCompilation.initState(
    name: String,
    entrypoint: IRStatement
): MutableMap<IRRegister, AccessSource> {
    val numGlobals = numGlobals // cache value to avoid extra work
    val numParams = functions[name]?.numParams ?: 0

    val state: MutableMap<IRRegister, AccessSource> = entrypoint.followChain()
        .asSequence()
        .flatMap { it.params }
        .associateWithTo(mutableMapOf()) { AccessSource.Undefined }

    // populate state with globals and params
    if (name != TOPLEVEL_ENTRYPOINT_NAME) {
        repeat(numGlobals) {
            state[IRRegister(it.toUInt())] = AccessSource.Global(it)
        }
    }
    repeat(numParams) {
        state[IRRegister((numGlobals + it).toUInt())] = AccessSource.Parameter(name, it)
    }

    return state
}
