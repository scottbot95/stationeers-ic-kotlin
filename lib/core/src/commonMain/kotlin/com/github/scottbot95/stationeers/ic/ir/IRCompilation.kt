package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.ir.IRStatement.ConditionalStatement

data class IRFunction(
    val name: String,
    val entrypoint: IRStatement,
    val numParams: Int,
)

data class IRCompilation(
    val functions: Map<String, IRFunction>,
    val topLevel: IRStatement,
    val allStatements: List<IRStatement>,
) : Iterable<IRStatement> {
    override operator fun iterator(): Iterator<IRStatement> = iterator {
        var labelCount = 0

        class Data(
            var labelOverride: String? = null,
        ) {
            var done: Boolean = false
            var referred: Boolean = false
            var needsLabel: Boolean = false
            val label by lazy { labelOverride ?: "L${labelCount++}" }
        }

        val statistics: MutableMap<IRStatement, Data> = mutableMapOf()
        val remainingStatements: MutableList<IRStatement> = mutableListOf()

        remainingStatements += topLevel
        statistics[topLevel] = Data(labelOverride = "start").apply {
            needsLabel = true
        }

        // Add entrypoints to process queue
        functions.forEach { (name, func) ->
            remainingStatements += func.entrypoint
            statistics[func.entrypoint] = Data(labelOverride = name).apply {
                needsLabel = true
            }
        }

        // create labels for any line we will potentially need to jump to
        allStatements.forEach {
            it.next?.let { next ->
                val data = statistics.getOrPut(next) { Data() }
                if (data.referred) {
                    // need to jump if multiple statements have a `next` pointing to this statement
                    data.needsLabel = true
                }
                data.referred = true
            }
            (it as? ConditionalStatement)?.cond?.let { cond ->
                val data = statistics.getOrPut(cond) { Data() }
                // Always need a label for branches
                data.needsLabel = true
                it.jumpLabel = data.label
            }
        }

        // process queue
        while (remainingStatements.isNotEmpty()) {
            var needsJump = false
            // follow this chain until the end
            for (statement in remainingStatements.removeFirst().followNext(false)) {
                val stats = statistics[statement]!!
                if (stats.done) {
                    if (needsJump) {
                        yield(IRStatement.Jump(stats.label))
                    }
                    break
                }
                stats.done = true

                if (stats.needsLabel) yield(IRStatement.Label(stats.label))

                // Label statements *should* only be function entrypoints so will already have a label metadata attached
                // no need to label
                if (statement !is IRStatement.Label) yield(statement)

                statement.cond?.let { cond ->
                    val branchStats = statistics[cond]!!
                    // add this branch to the work queue if not already processed
                    if (!branchStats.done) {
                        remainingStatements.add(0, cond)
                    }
                }

                needsJump = true
            }
        }
    }
}
