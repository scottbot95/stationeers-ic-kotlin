package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.util.DefaultingMutableMap
import com.github.scottbot95.stationeers.ic.util.DefaultingMutableMapImpl

fun IRCompilation.compilationIterator(): Iterator<IRStatement> = iterator {
    var labelCount = 0

    class Data(
        var labelOverride: String? = null,
    ) {
        var done: Boolean = false
        var referred: Boolean = false
        var needsLabel: Boolean = false
            set(value) {
                field = value
                label // eager init to reserve label count early
            }
        val label by lazy { labelOverride ?: "L${labelCount++}" }
    }

    val statistics: DefaultingMutableMap<IRStatement, Data> = DefaultingMutableMapImpl { Data() }
    val remainingStatements: MutableList<IRStatement> = mutableListOf()

    allEntrypoints.forEach { (name, statement) ->
        remainingStatements += statement
        statistics[statement] = Data(labelOverride = name).apply {
            needsLabel = true
        }
    }

    // create labels for any line we will potentially need to jump to
    allStatements.forEach {
        it.next?.let { next ->
            val data = statistics[next]
            if (data.referred) {
                // need to jump if multiple statements have a `next` pointing to this statement
                data.needsLabel = true
            }
            data.referred = true
        }
        (it as? IRStatement.ConditionalStatement)?.cond?.let { cond ->
            val data = statistics[cond]
            // Always need a label for branches
            data.needsLabel = true
            it.jumpLabel = data.label
        }
    }

    val visitedStatements = mutableListOf<IRStatement>()

    // process queue
    while (remainingStatements.isNotEmpty()) {
        var needsJump = false
        // follow this chain until the end
        for (statement in remainingStatements.removeFirst().followNext(false)) {
            visitedStatements += statement
            // Create stats for references statements

            val stats = statistics[statement]
            if (stats.done) {
                if (needsJump) {
                    yield(IRStatement.Jump(stats.label))
                }
                break
            }
            stats.done = true

            if (stats.needsLabel) yield(IRStatement.Label(stats.label))

            yield(statement)

            statement.cond?.let { cond ->
                val branchStats = statistics[cond]
                // add this branch to the work queue if not already processed
                if (!branchStats.done) {
                    remainingStatements.add(0, cond)
                }
            }

            needsJump = true
        }
    }
}

data class IREntrypoint(
    val label: String,
    val statement: IRStatement,
)

const val TOPLEVEL_ENTRYPOINT_NAME = "start"

val IRCompilation.allEntrypoints: Iterable<IREntrypoint>
    get() = Iterable {
        iterator {
            yield(IREntrypoint(TOPLEVEL_ENTRYPOINT_NAME, topLevel))
            yieldAll(functions.entries.map { IREntrypoint(it.key, it.value.entrypoint) })
        }
    }

val IRCompilation.allStatements: Sequence<IRStatement>
    get() = allEntrypoints.asSequence().flatMap { it.statement.followChain() }

/**
 * Calculate some stats describing this compilation.
 *
 * Note: This can be fairly expensive for larger scripts
 */
fun IRCompilation.stats(): IRCompilation.Stats {
    val allStatements = iterator().asSequence().toList()

    val numBranches = allStatements.count { it is IRStatement.ConditionalStatement }

    // can we do something fancier here?
    val complexityRating = numBranches.toDouble()

    return IRCompilation.Stats(
        totalLines = allStatements.size,
        minLines = allStatements.count { it !is IRStatement.Label },
        numFunctions = functions.size,
        complexity = complexityRating,
    )
}
