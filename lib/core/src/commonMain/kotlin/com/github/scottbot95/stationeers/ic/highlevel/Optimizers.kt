package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.util.mapDepthFirst

private val allOptimizations = listOf(ConstantFoldingOptimizer)

class Optimizer(
    private val optimizations: List<Optimization> = allOptimizations,
) {
    fun optimizeTree(expr: Expression): Expression {
        return expr.mapDepthFirst {
            val optimizedExpr = optimizations.fold(it) { toOptimize, optimization ->
                optimization.optimize(toOptimize)
            }

            optimizedExpr
        }
    }
}

interface Optimization {
    fun optimize(expr: Expression): Expression
}

object ConstantFoldingOptimizer : Optimization {
    override fun optimize(expr: Expression): Expression = when (expr) {
        is Expression.Negate -> when (expr.expr) {
            is Expression.IntLiteral -> Expression.IntLiteral(-expr.expr.value)
            is Expression.FloatLiteral -> Expression.FloatLiteral(-expr.expr.value)
            is Expression.Negate -> expr.expr.expr
            else -> expr
        }
        is Expression.Equals -> when {
            expr.a is Expression.NumberLiteral<*> && expr.b is Expression.NumberLiteral<*> -> {
                Expression.IntLiteral(if (expr.a.value == expr.b.value) 1 else 0)
            }
            expr.a == expr.b -> Expression.IntLiteral(1)
            else -> expr
        }
        else -> expr
    }

}
