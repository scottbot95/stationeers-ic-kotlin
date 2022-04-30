package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.AccessInfo
import com.github.scottbot95.stationeers.ic.ir.AccessSource
import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRRegister
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.generateAccessInfo
import com.github.scottbot95.stationeers.ic.ir.reachableFrom
import com.github.scottbot95.stationeers.ic.ir.replaceWith
import com.github.scottbot95.stationeers.ic.util.isFalsy
import com.github.scottbot95.stationeers.ic.util.isTruthy
import com.github.scottbot95.stationeers.ic.util.plus
import com.github.scottbot95.stationeers.ic.util.toInt
import mu.KotlinLogging

object ConstantSimplification : IROptimization {
    private val logger = KotlinLogging.logger { }

    override fun optimize(compilation: IRCompilation): Boolean {
        repeat(2) { round ->
            val accessInfo = compilation.generateAccessInfo(true, round == 1)
            val changes: Int = accessInfo.count { (statement, info) ->
                when (statement) {
                    is IRStatement.Negate -> info.getLiteral(statement, statement.src)?.let { literal ->
                        statement replaceWith IRStatement.Init(statement.dest, null, -literal.toFloat())
                        true
                    }
                    is IRStatement.Add -> {
                        val a = info.getLiteral(statement, statement.a)
                        val b = info.getLiteral(statement, statement.b)
                        when {
                            a != null && b != null -> statement replaceWith IRStatement.Init(
                                statement.dest,
                                null,
                                a + b
                            )
                            a.isFalsy -> statement replaceWith IRStatement.Copy(statement.dest, statement.b)
                            b.isFalsy -> statement replaceWith IRStatement.Copy(statement.dest, statement.a)
                            else -> false
                        }
                    }
                    is IRStatement.Equals -> if (info[statement.a]?.writers == info[statement.b]?.writers) {
                        statement replaceWith IRStatement.Init(statement.dest, null, 1)
                    } else {
                        val a = info.getLiteral(statement, statement.a)
                        val b = info.getLiteral(statement, statement.b)
                        if (a != null && b != null) statement replaceWith IRStatement.Init(
                            statement.dest,
                            null,
                            (a == b).toInt()
                        )
                        else false
                    }
                    is IRStatement.ConditionalStatement -> info.getLiteral(statement, statement.check, false)?.let {
                        // If the value of the test register is known at compile time,
                        // change the branch into a nop and choose the appropriate next pointer
                        logger.debug { "Constant branch elided (type 2)" }
                        if (statement.matches(it)) {
                            statement.next = statement.cond
                        }
                        statement replaceWith IRStatement.Nop()
                    }
                    // If at point of init there exists another register with the same value,
                    // replace with a copy of that register. This enables other optimizations to potentially remove the copy
                    is IRStatement.Init -> info.keys.takeIf { statement.ident == null }
                        ?.firstOrNull { info.getLiteral(statement, it) == statement.value }
                        ?.let { statement replaceWith IRStatement.Copy(statement.dest, it) }
                    else -> false
                } ?: false
            }

            if (changes != 0) {
                logger.debug { "$changes literal expressions simplified" }
                return true
            }
        }
        return false
    }

    private fun AccessInfo.getLiteral(
        statement: IRStatement,
        register: IRRegister,
        exactValue: Boolean = true
    ): Number? {
        var result: Number? = null
        this[register]?.writers?.forEach { source ->
            if (source !is AccessSource.Statement || source.statement == statement) return null
            val writer = source.statement
            val value = when {
                writer is IRStatement.Init && writer.dest == register -> if (writer.ident == null) {
                    if (exactValue) writer.value else writer.value.isTruthy.toInt()
                } else null
                (writer is IRStatement.ConditionalStatement) && writer.check == register -> {
                    // Can only infer value for truthy/falsy branches
                    if (writer !is IRStatement.IfNotZero && writer !is IRStatement.IfZero) return null
                    val (truthyPath, falsyPath) = if (writer is IRStatement.IfNotZero) {
                        writer.cond to writer.next
                    } else {
                        writer.next to writer.cond
                    }
                    // test register must have been truthy if we can get here from the cond branch
                    val regIsTruthy = statement reachableFrom truthyPath!!
                    // can't infer anything if both paths lead here (or neither do)
                    if (regIsTruthy == (statement reachableFrom falsyPath!!)) return null
                    // if exact value is needed, verify that all sources to the condition
                    // are Equals statements (where only possible nonzero outcome is 1)
                    if (regIsTruthy && exactValue && this[writer.check]!!.writers.any { (it as? AccessSource.Statement)?.statement !is IRStatement.Equals }) {
                        null
                    } else regIsTruthy.toInt()
                }

                else -> null
            }

            // If sources disagree on the value, we can't determine it at compile time
            if (result != null && value != null && result != value) return null
            result = value
        }

        return result
    }
}
