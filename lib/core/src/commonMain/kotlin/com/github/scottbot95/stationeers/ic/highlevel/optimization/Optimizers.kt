package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Add
import com.github.scottbot95.stationeers.ic.highlevel.Expression.And
import com.github.scottbot95.stationeers.ic.highlevel.Expression.CompoundExpression
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Copy
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Equals
import com.github.scottbot95.stationeers.ic.highlevel.Expression.FloatLiteral
import com.github.scottbot95.stationeers.ic.highlevel.Expression.FunctionCall
import com.github.scottbot95.stationeers.ic.highlevel.Expression.IntLiteral
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Loop
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Negate
import com.github.scottbot95.stationeers.ic.highlevel.Expression.NoOp
import com.github.scottbot95.stationeers.ic.highlevel.Expression.NumberLiteral
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Or
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Return
import com.github.scottbot95.stationeers.ic.highlevel.ICFunction
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.highlevel.updatePureFunctions
import com.github.scottbot95.stationeers.ic.util.isFalsy
import com.github.scottbot95.stationeers.ic.util.isTruthy
import com.github.scottbot95.stationeers.ic.util.mapDepthFirst

private val allOptimizations = listOf(
    OperatorCompression,
    ConstantFolding,
    ShortCircuiting,
    DeadCodeElimination,
    OperatorPruning,
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
        is Add, is CompoundExpression, is Or, is And -> {
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
        is Add -> {
            val literalChildren = expr.children.filterIsInstance<NumberLiteral<*>>()

            // Sum up all compile-time constants
            val literalSum = if (literalChildren.all { it is IntLiteral }) {
                IntLiteral(literalChildren.sumOf { it.value.toInt() })
            } else {
                FloatLiteral(literalChildren.sumOf { it.value.toDouble() }.toFloat())
            }

            val otherChildren = expr.children
                .filter { it !is NumberLiteral<*> }
                .flatMap { child ->
                    when {
                        // Adopt children of negate
                        child is Negate && child.expr is Add -> child.expr.children.map {
                            Negate(it)
                        }
                        else -> listOf(child)
                    }
                }

            val newChildren = otherChildren + if (literalSum.value.toFloat() != 0f) listOf(literalSum) else emptyList()

            // if greater than half are inverted, flip the inversion and invert sum
            if (otherChildren.count { it is Negate } > otherChildren.size / 2) {
                Negate(expr.copy(
                    children = newChildren.map { Negate(it) }
                ))
            } else {
                expr.copy(
                    children = newChildren
                )
            }
        }
        is Negate -> when (expr.expr) {
            // literals can be negated at compile time
            is IntLiteral -> IntLiteral(-expr.expr.value)
            is FloatLiteral -> FloatLiteral(-expr.expr.value)
            // Double negate does nothing
            is Negate -> expr.expr.expr
            else -> expr
        }
        is Equals -> when {
            // comparisons between literals can be done at compile time
            expr.a is NumberLiteral<*> && expr.b is NumberLiteral<*> -> {
                IntLiteral(if (expr.a.value == expr.b.value) 1 else 0)
            }
            // comparisons between equivalent expressions can be simplified
            expr.a == expr.b -> IntLiteral(1)
            else -> expr
        }
        is And, is Or -> {
            // strip useless children (non-zero for && and zero ||)
            fun valueKind(num: Number) = if (expr is And) num.isTruthy else num.isFalsy
            val usefulChildren = expr.children.filterNot { it is NumberLiteral<*> && valueKind(it.value) }

            expr.copy(
                children = usefulChildren
            )
        }
        is Copy -> when {
            // If an assign statement assigns to itself and the expression has no side effects, replace with LHS
            expr.source == expr.destination && expr.source.isPure(context) -> expr.source
            else -> expr
        }
        is Loop -> when {
            expr.condition is NumberLiteral<*> && expr.condition.value.isFalsy -> NoOp
            else -> expr
        }
        else -> expr
    }

}

object ShortCircuiting : Optimization {
    override fun optimize(expr: Expression, context: ICScriptContext): Expression = when (expr) {
        is And, is Or -> {
            fun valueKind(num: Number) = if (expr is And) num.toFloat() != 0f else num.toFloat() == 0f
            // If terminating literal found (zero for && and non-zero for ||), delete all params after and all preceding pure params
            val terminatingIndex = expr.children.indexOfFirst { it is NumberLiteral<*> && !valueKind(it.value) }
            if (terminatingIndex != -1) {
                CompoundExpression(
                    expr.copy(children = expr.children.take(terminatingIndex).dropLastWhile { it.isPure(context) }),
                    if (expr is And) 0.toExpr() else 1.toExpr()
                )

            } else {
                expr
            }
        }
        else -> expr
    }

}

object DeadCodeElimination : Optimization {
    override fun optimize(expr: Expression, context: ICScriptContext): Expression = when (expr) {
        is CompoundExpression -> {
            // strip all code after a return or infinite loop
            var hasReturned = false
            val liveChildren = expr.children
                .filterIndexed { i, child -> i == expr.children.size - 1 || !child.isPure(context) }
                .takeWhile { child ->
                    !hasReturned.also {
                        hasReturned = child is Return ||
                                (child is Loop && child.condition is NumberLiteral<*> && child.condition.value.isTruthy)
                    }
                }

            // Adopt members of any sub-expression where the result is not needed
            val flattenedChildren = liveChildren.flatMapIndexed { i, child ->
                if (i == liveChildren.size - 1) listOf(child)
                else when (child) {
                    is FunctionCall, is Add, is Negate, is Equals, is CompoundExpression -> {
                        if (child is FunctionCall && context.functions[child.function.ident.index].pure != true) {
                            listOf(child)
                        } else {
                            // adopt grand-children
                            child.children
                        }
                    }
                    else -> listOf(child)
                }
            }

            // If the last element is the same as the LHS of a preceding assignment, delete the last element.
            // eg: x = (a=3, a) => x= (a=3)
            val newChildren = if (flattenedChildren.size >= 2) {
                val prev = flattenedChildren[flattenedChildren.size - 2]
                if (prev is Copy && flattenedChildren.last() == prev.destination) {
                    flattenedChildren.dropLast(1)
                } else {
                    flattenedChildren
                }
            } else {
                flattenedChildren
            }

            CompoundExpression(newChildren)
        }
        else -> expr
    }
}

object OperatorPruning : Optimization {

    // If type is or, and, or add and there remains only one operand, replace with operand
    override fun optimize(expr: Expression, context: ICScriptContext): Expression = when {
        expr.children.size == 1 -> when (expr) {
            is Or, is And, is Add, is CompoundExpression -> expr.children.first()
            else -> expr
        }
        expr.children.isEmpty() -> when (expr) {
            is Or, is Add -> 0.toExpr()
            is And -> 1.toExpr()
            is CompoundExpression -> NoOp
            else -> expr
        }
        else -> expr
    }
}
