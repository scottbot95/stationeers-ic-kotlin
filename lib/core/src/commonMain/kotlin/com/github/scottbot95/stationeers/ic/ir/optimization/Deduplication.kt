package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.allStatements
import com.github.scottbot95.stationeers.ic.ir.replaceWith
import com.github.scottbot95.stationeers.ic.util.toInt
import mu.KotlinLogging

/**
 * Merge equivalent statement trees.
 *
 * Two statements are considered equivalent if:
 *   - Their next/cond pointers point to the same statement(s)
 *   - They are the same statement type, and have equivalent operands
 */
object Deduplication : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        var changes = 0
        compilation.allStatements.forEach { statement ->
            if (statement.prev.size > 1) {
                // make copy since we'll potentially be changing this list
                val prev = statement.prev.toList()
                // multiple statements point to this one. Check if any of them are duplicates
                prev.forEachIndexed { i, a ->
                    prev.drop(i + 1).forEach { b ->
                        if (a::class == b::class && a.params == b.params) {
                            // matching statements. Redirect b.next to point to a
                            changes += b.replaceWith(a).toInt()
                        }
                    }
                }
            }
        }

        logger.debug { "$changes duplicate statements merged." }
        return changes > 0
    }
}
