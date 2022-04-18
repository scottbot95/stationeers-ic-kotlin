package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.Types
import com.github.scottbot95.stationeers.ic.highlevel.defVar
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.matchers.should

class ConditionalOptimizations : OptimizationTest(body = {
    "Conditional Optimizations" should {
        "simplify nested and expressions" {
            val x = context.defVar("x", Types.Int)
            val expr = Expression.CompoundExpression(
                Expression.And(
                    Expression.And(
                        Expression.And(
                            Expression.And(
                                Expression.And(
                                    Expression.And(
                                        Expression.And(
                                            context.defVar("a", Types.Int),
                                            context.defVar("b", Types.Int)
                                        ),
                                        1.toExpr()
                                    ),
                                    Expression.Copy(x, context.defVar("c", Types.Int))
                                ),
                                context.defVar("d", Types.Int)
                            ),
                            0.toExpr()
                        ),
                        context.defVar("e", Types.Int)
                    ),
                    Expression.CompoundExpression(
                        Expression.Copy(1.toExpr(), x)
                    )
                ),
                Expression.Return(x)
            )

            val optimized = optimizeExpr(expr)

            optimized.toTreeString() should matchSnapshot
        }

        "simplify nested or expression" {
            val x = context.defVar("x", Types.Int)
            val expr = Expression.CompoundExpression(
                Expression.And(
                    Expression.Or(
                        Expression.Or(
                            Expression.Or(
                                Expression.Or(
                                    Expression.Or(
                                        Expression.Or(
                                            context.defVar("a", Types.Int),
                                            context.defVar("b", Types.Int)
                                        ),
                                        0.toExpr()
                                    ),
                                    Expression.Copy(x, context.defVar("c", Types.Int))
                                ),
                                context.defVar("d", Types.Int)
                            ),
                            1.toExpr()
                        ),
                        context.defVar("e", Types.Int)
                    ),
                    Expression.CompoundExpression(
                        Expression.Copy(1.toExpr(), x)
                    )
                ),
                Expression.Return(x)
            )

            val optimized = optimizeExpr(expr)

            optimized.toTreeString() should matchSnapshot
        }
    }
})
