package com.github.scottbot95.stationeers.ic.ir

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
        (it as? IRStatement.ConditionalStatement)?.cond?.let { cond ->
            val data = statistics.getOrPut(cond) { Data() }
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

val IRCompilation.allEntrypoints: Iterable<IRStatement>
    get() = Iterable {
        iterator {
            yield(topLevel)
            yieldAll(functions.values.map { it.entrypoint })
        }
    }

val IRCompilation.allStatements: Sequence<IRStatement>
    get() = allEntrypoints.asSequence().flatMap { it.followChain() }


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
        allStatements.size,
        allStatements.count { it !is IRStatement.Label },
        complexityRating
    )
}