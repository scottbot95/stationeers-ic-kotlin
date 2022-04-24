package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.allStatements
import com.github.scottbot95.stationeers.ic.ir.replace
import com.github.scottbot95.stationeers.ic.util.toInt
import mu.KotlinLogging

object TrimNoOps : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        val elisions = compilation.allStatements.sumOf {
            // Delete the next pointer on any return statements
            if (it is IRStatement.Return && it.next != null) it.next = null
            reduceNopChain(it).toInt()
        }

        if (elisions != 0) logger.debug { "$elisions NOPs elided." }

        return elisions != 0
    }

    private fun reduceNopChain(statement: IRStatement): Boolean {
        if (isNop(statement) && statement.prev.isNotEmpty()) {
            statement.replace(statement.next)
            return true
        }

        return false
    }

    private fun isNop(statement: IRStatement): Boolean = (statement is IRStatement.Nop) ||
        (statement is IRStatement.ConditionalStatement && (statement.next == statement.cond || statement.cond == null)) ||
        (statement is IRStatement.Copy && statement.src == statement.dest)
}
