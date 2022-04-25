package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.allEntrypoints
import com.github.scottbot95.stationeers.ic.ir.cond
import com.github.scottbot95.stationeers.ic.ir.replaceWith
import com.github.scottbot95.stationeers.ic.util.toInt
import mu.KotlinLogging
import kotlin.reflect.KMutableProperty0

object JumpThreading : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        var changes = 0

        compilation.allEntrypoints.forEach { (_, statement) ->
            changes += statement.optimizeRegisterReuse { this::next }
            changes += statement.optimizeRegisterReuse { this::cond }
            changes += statement.optimizeConstantCondition()
            changes += statement.optimizeCopyReturn()
        }

        return changes > 0
    }

    /**
     * If a test for a register is immediately followed by another test for the same register, skip the second test
     */
    private fun IRStatement.optimizeRegisterReuse(
        next: IRStatement.() -> KMutableProperty0<IRStatement?>
    ): Int {
        if (this !is IRStatement.ConditionalStatement) return 0

        var changes = 0
        val nextProp = next()

        while (
            (this::class == nextProp.get()?.let { it::class }) &&
            (check == (nextProp as? IRStatement.ConditionalStatement)?.check) &&
            (nextProp != nextProp.get()?.next())
        ) {
            // Checks match so outcomes with match
            nextProp.set(nextProp.get()?.next()?.get())
            logger.debug { "$opCode -> ${nextProp.get()?.opCode} `${nextProp.name}` case threaded" }
            changes++
        }

        return changes
    }

    /**
     * If a literal load is immediately followed by a conditional jump, hardcode the jump
     */
    private fun IRStatement.optimizeConstantCondition(): Int {
        if (this !is IRStatement.Init || ident != null) return 0

        var changes = 0

        while ((next as? IRStatement.ConditionalStatement)?.check == reg) {
            next = if ((next as IRStatement.ConditionalStatement).matches(value)) {
                next?.cond
            } else {
                next?.next
            }
            logger.debug { "Constant conditional jump elided." }
            changes++
        }

        return changes
    }

    /**
     * If a copy is followed by a return that consumes the copy result, change the copy into a return.
     * This leaves an extra return, but that will be cleaned up the by the nop trimming
     *
     * TODO this doesn't really fit with jump threading. Find somewhere else to put it?
     */
    private fun IRStatement.optimizeCopyReturn(): Int =
        if (this is IRStatement.Copy && (next as? IRStatement.Return)?.result == dest) {
            replaceWith(IRStatement.Return(src)).toInt()
        } else {
            0
        }
}
