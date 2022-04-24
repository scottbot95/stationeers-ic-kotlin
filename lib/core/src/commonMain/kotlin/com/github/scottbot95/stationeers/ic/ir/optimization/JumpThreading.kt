package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.allEntrypoints
import com.github.scottbot95.stationeers.ic.ir.cond
import mu.KotlinLogging
import kotlin.reflect.KMutableProperty0

object JumpThreading : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        var changes = 0

        compilation.allEntrypoints.forEach { statement ->
            changes += optimizeRegisterReuse(statement) { this::next }
            changes += optimizeRegisterReuse(statement) { this::cond }
            changes += optimizeConstantCondition(statement)
        }

        return changes > 0
    }

    /**
     * If a test for a register is immediately followed by another test for the same register, skip the second test
     */
    private fun optimizeRegisterReuse(
        statement: IRStatement,
        next: IRStatement.() -> KMutableProperty0<IRStatement?>
    ): Int {
        var changes = 0
        val nextProp = statement.next()

        while (
            (statement is IRStatement.ConditionalStatement) &&
            (statement::class == nextProp.get()?.let { it::class }) &&
            (statement.check == (nextProp as? IRStatement.ConditionalStatement)?.check) &&
            (nextProp != nextProp.get()?.next())
        ) {
            // Checks match so outcomes with match
            nextProp.set(nextProp.get()?.next()?.get())
            logger.debug { "${statement.opCode} -> ${nextProp.get()?.opCode} `${nextProp.name}` case threaded" }
            changes++
        }

        return changes
    }

    /**
     * If a literal load is immediately followed by a conditional jump, hardcode the jump
     */
    private fun optimizeConstantCondition(statement: IRStatement): Int {
        var changes = 0

        while (
            (statement is IRStatement.Init) &&
            (statement.ident == null) &&
            ((statement.next as? IRStatement.ConditionalStatement)?.check == statement.reg)
        ) {
            statement.next = if ((statement.next as IRStatement.ConditionalStatement).matches(statement.value)) {
                statement.next?.cond
            } else {
                statement.next?.next
            }
            logger.debug { "Constant conditional jump elided." }
            changes++
        }

        return changes
    }
}
