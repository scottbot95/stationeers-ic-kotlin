package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICFunction
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel
import com.github.scottbot95.stationeers.ic.highlevel.updatePureFunctions
import com.github.scottbot95.stationeers.ic.util.mapDepthFirst

private val allOptimizations = listOf(
    OperatorCompression,
    ConstantFolding
)

class Optimizer(
    private val optimizations: List<Optimization> = allOptimizations,
    private val maxAttempts: Int = 100,
) {
    fun optimizeTopLevel(topLevel: ICScriptTopLevel): ICScriptTopLevel {
        fun optimizeExpr(expr: Expression, functions: List<ICFunction>): Expression {
            topLevel.updatePureFunctions()
            return optimizeTree(expr, ICScriptContext(functions = functions.toMutableList()))
        }

        var oldTopLevel = topLevel

        repeat(maxAttempts) {
            val optimizedFunctions = oldTopLevel.functions.map {
                it.copy(code = optimizeExpr(it.code, oldTopLevel.functions))
            }

            val optimizedCode = optimizeExpr(oldTopLevel.code, optimizedFunctions)

            val newTopLevel = oldTopLevel.copy(
                functions = optimizedFunctions,
                code = optimizedCode,
            )

            if (newTopLevel == oldTopLevel) {
                return newTopLevel
            } else {
                oldTopLevel = newTopLevel
            }
        }

        // TODO warn about optimizer exceeding max attempts
        return oldTopLevel
    }

    fun optimizeTree(expr: Expression, context: ICScriptContext): Expression {
        return expr.mapDepthFirst {
            val optimizedExpr = optimizations.fold(it) { toOptimize, optimization ->
                optimization.optimize(toOptimize, context)
            }

            optimizedExpr
        }
    }
}

interface Optimization {
    fun optimize(expr: Expression, context: ICScriptContext): Expression
}

object OperatorCompression : Optimization {
    override fun optimize(expr: Expression, context: ICScriptContext): Expression = when (expr) {
        is Expression.Add, is Expression.CompoundExpression, is Expression.Or, is Expression.And -> {
            // Adopt all children of same type
            val addedChildren = expr.children.flatMap { child ->
                if (expr::class.isInstance(child)) {
                    child.children
                } else {
                    emptyList()
                }
            }

            if (addedChildren.isNotEmpty()) {
                expr.copy(children = expr.children.filter { !expr::class.isInstance(it) } + addedChildren)
            } else {
                expr
            }
        }
        else -> expr
    }

}

object ConstantFolding : Optimization {
    override fun optimize(expr: Expression, context: ICScriptContext): Expression = when (expr) {
        is Expression.Add -> {
            val literalChildren = expr.children.filterIsInstance<Expression.NumberLiteral<*>>()

            // Sum up all compile-time constants
            val literalSum = if (literalChildren.all { it is Expression.IntLiteral }) {
                Expression.IntLiteral(literalChildren.sumOf { it.value.toInt() })
            } else {
                Expression.FloatLiteral(literalChildren.sumOf { it.value.toDouble() }.toFloat())
            }

            val otherChildren = expr.children
                .filter { it !is Expression.NumberLiteral<*> }
                .flatMap { child ->
                    when {
                        // Adopt children of negate
                        child is Expression.Negate && child.expr is Expression.Add -> child.expr.children.map {
                            Expression.Negate(it)
                        }
                        else -> listOf(child)
                    }
                }

            val newChildren = otherChildren + if (literalSum.value.toFloat() != 0f) listOf(literalSum) else emptyList()

            // if greater than half are inverted, flip the inversion and invert sum
            if (otherChildren.count { it is Expression.Negate } > otherChildren.size / 2) {
                Expression.Negate(expr.copy(
                    children = newChildren.map { Expression.Negate(it) }
                ))
            } else {
                expr.copy(
                    children = newChildren
                )
            }
        }
        is Expression.Negate -> when (expr.expr) {
            // literals can be negated at compile time
            is Expression.IntLiteral -> Expression.IntLiteral(-expr.expr.value)
            is Expression.FloatLiteral -> Expression.FloatLiteral(-expr.expr.value)
            // Double negate does nothing
            is Expression.Negate -> expr.expr.expr
            else -> expr
        }
        is Expression.Equals -> when {
            // comparisons between literals can be done at compile time
            expr.a is Expression.NumberLiteral<*> && expr.b is Expression.NumberLiteral<*> -> {
                Expression.IntLiteral(if (expr.a.value == expr.b.value) 1 else 0)
            }
            // comparisons between equivalent expressions can be simplified
            expr.a == expr.b -> Expression.IntLiteral(1)
            else -> expr
        }
        is Expression.Copy -> when {
            // If an assign statement assigns to itself and the expression has no side effects, replace with LHS
            expr.source == expr.destination && expr.source.isPure(context) -> expr.source
            else -> expr
        }
        is Expression.Loop -> when {
            expr.condition is Expression.NumberLiteral<*> && expr.condition.value == 0 -> Expression.NoOp
            else -> expr
        }
        else -> expr
    }

}
