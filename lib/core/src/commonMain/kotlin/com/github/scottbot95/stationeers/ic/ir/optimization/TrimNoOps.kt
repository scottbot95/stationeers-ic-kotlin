package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.AccessInfo
import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.allStatements
import com.github.scottbot95.stationeers.ic.ir.generateAccessInfo
import com.github.scottbot95.stationeers.ic.ir.replaceWith
import com.github.scottbot95.stationeers.ic.util.toInt
import mu.KotlinLogging

object TrimNoOps : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        val accessInfo = compilation.generateAccessInfo(false)

        val elisions = compilation.allStatements.sumOf {
            // Delete the next pointer on any return statements
            if (it is IRStatement.Return && it.next != null) it.next = null
            reduceNopChain(accessInfo, it).toInt()
        }

        if (elisions != 0) logger.debug { "$elisions NOPs elided." }

        return elisions != 0
    }

    private fun reduceNopChain(accessInfo: Map<IRStatement, AccessInfo>, statement: IRStatement): Boolean {
        if (isNop(accessInfo, statement)) {
            return statement.replaceWith(statement.next)
        }

        return false
    }

    private fun isNop(accessInfo: Map<IRStatement, AccessInfo>, statement: IRStatement): Boolean =
        (statement is IRStatement.Nop) ||
            (statement is IRStatement.ConditionalStatement && (statement.next == statement.cond || statement.cond == null)) ||
            (statement is IRStatement.Copy && statement.src == statement.dest) ||
            (statement is IRStatement.WritingStatement && accessInfo[statement]?.get(statement.dest)?.reads?.isNotEmpty() != true)
}
