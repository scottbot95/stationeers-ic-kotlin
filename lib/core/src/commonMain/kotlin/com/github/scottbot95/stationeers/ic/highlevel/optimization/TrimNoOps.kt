package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.allEntrypoints
import com.github.scottbot95.stationeers.ic.ir.followChain
import com.github.scottbot95.stationeers.ic.ir.replace
import com.github.scottbot95.stationeers.ic.util.toInt
import mu.KotlinLogging

object TrimNoOps : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        // Delete the next pointer on any return statements
        compilation.allStatements.forEach {
            if (it is IRStatement.Return && it.next != null) it.next = null
        }
//        val elisions = reduceNopChain(compilation.topLevel).compareTo(false) +
//                compilation.functions.values.sumOf { reduceNopChain(it.entrypoint).compareTo(false) }
        val elisions = compilation.allEntrypoints.asSequence().flatMap { it.followChain() }.sumOf {
            reduceNopChain(it).toInt()
        }

        if (elisions != 0) logger.debug { "$elisions NOPs elided." }

        return elisions != 0
    }

    private fun reduceNopChain(statement: IRStatement): Boolean {
        if (isNop(statement)) {
            statement.replace(statement.next)
            return true
        }

        return false
    }

    private fun isNop(statement: IRStatement): Boolean = (statement is IRStatement.Nop) ||
            (statement is IRStatement.ConditionalStatement && (statement.next == statement.cond || statement.cond == null)) ||
            (statement is IRStatement.Copy && statement.src == statement.dest)
}
