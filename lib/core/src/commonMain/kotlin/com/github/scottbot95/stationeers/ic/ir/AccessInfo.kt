package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.util.DefaultingMutableMap
import com.github.scottbot95.stationeers.ic.util.DefaultingMutableMapImpl
import com.github.scottbot95.stationeers.ic.util.toInt
import kotlin.jvm.JvmInline

sealed interface AccessSource {
    object Undefined : AccessSource

    @JvmInline
    value class Statement(val statement: IRStatement) : AccessSource

    data class Global(val index: Int) : AccessSource
    data class Parameter(val functionName: String, val paramIndex: Int) : AccessSource
}

typealias StateType = DefaultingMutableMap<IRRegister, MutableSet<AccessSource>>

data class AccessInfo(
    /**
     * Access info for each parameter
     */
    var params: StateType,
    /**
     * Access info for all registers
     */
    val presence: StateType,
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
    // Realize statement list
    val allStatements = allStatements.toList()

    val data: DefaultingMutableMap<IRStatement, AccessInfo> = DefaultingMutableMapImpl {
        AccessInfo(
            params = DefaultingMutableMapImpl { mutableSetOf(AccessSource.Undefined) },
            presence = DefaultingMutableMapImpl { mutableSetOf(AccessSource.Undefined) },
        )
    }

    val numGlobals = numGlobals // cache value to avoid extra work
    // begin at each entrypoint
    allEntrypoints.forEach { (name, statement) ->
        val numParams = functions[name]?.numParams ?: 0

        val state: StateType = DefaultingMutableMapImpl {
            mutableSetOf(AccessSource.Undefined)
        }

        // populate state with globals and params
        if (name != TOPLEVEL_ENTRYPOINT_NAME) {
            repeat(numGlobals) {
                state[IRRegister(it.toUInt())] = mutableSetOf(AccessSource.Global(it))
            }
        }
        repeat(numParams) {
            state[IRRegister((numGlobals + it).toUInt())] = mutableSetOf(AccessSource.Parameter(name, it))
        }

        trace(statement, data, state, followCopies, includeBranchAsWriter)
    }

    return data.toMap()
}

private fun trace(
    statement: IRStatement,
    data: DefaultingMutableMap<IRStatement, AccessInfo>,
    state: StateType,
    followCopies: Boolean,
    includeBranchAsWriter: Boolean,
) {
    val myData = data[statement]
    var changes = 0

    // For this statement, add info about where the values in each register come from at this point
    changes += state.entries.sumOf { (reg, sources) ->
        sources.sumOf {
            myData.presence[reg].add(it).toInt()
        }
    }

    if (followCopies && statement is IRStatement.Copy) {
        if (changes == 0) return

        // After this statement, sources of dest are the same as sources of src
        state[statement.dest] = state[statement.src]
    } else {
        statement.readParams().forEach { reg ->
            changes += state[reg].count { source ->
                val writer = (source as? AccessSource.Statement)?.statement as? IRStatement.WritingStatement
                val writeDest = writer?.dest
                // Add write sources for all read registers
                val readParamsUpdated = myData.params[reg].add(source)

                // Add this statement as a read source on the writer's statement
                val writeParamUpdated =
                    writeDest == reg && data[writer].params[writeDest].add(AccessSource.Statement(statement))
                readParamsUpdated || writeParamUpdated
            }
        }

        if (changes == 0) return

        // Write regs only have this statement as a source after we execute
        (statement as? IRStatement.WritingStatement)?.dest?.let {
            state[it] = mutableSetOf(AccessSource.Statement(statement))
        }

        // If statement is a branch, we can infer the value of the test register. Optionally count this as a write
        if (includeBranchAsWriter && statement is IRStatement.ConditionalStatement) {
            state[statement.check] = mutableSetOf(AccessSource.Statement(statement))
        }
    }

    statement.cond?.let { cond ->
        val copy: DefaultingMutableMap<IRRegister, MutableSet<AccessSource>> =
            DefaultingMutableMapImpl(state.toMutableMap()) {
                mutableSetOf(AccessSource.Undefined) // TODO should try to merge this logic with generateAccessInfo
            }
        trace(cond, data, copy, followCopies, includeBranchAsWriter)
    }
    statement.next?.let { next ->
        trace(next, data, state, followCopies, includeBranchAsWriter)
    }
}
