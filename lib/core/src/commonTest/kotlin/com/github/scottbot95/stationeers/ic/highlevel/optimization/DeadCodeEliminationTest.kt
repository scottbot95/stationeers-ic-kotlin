package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel
import com.github.scottbot95.stationeers.ic.highlevel.Types
import com.github.scottbot95.stationeers.ic.highlevel.addFunction
import com.github.scottbot95.stationeers.ic.highlevel.defFunc
import com.github.scottbot95.stationeers.ic.highlevel.defVar
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.highlevel.toTreeString
import com.github.scottbot95.stationeers.ic.highlevel.use
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.datatest.withData
import io.kotest.matchers.should

class DeadCodeEliminationTest : OptimizationTest(
    body = {
        "DeadCodeElimination" should {
            "eliminate useless pure expressions" {
                val pureFunc = context.defFunc("pure_function", Types.Unit)
                val impureFunc = context.defFunc("has_side_effects_function", Types.Unit)

                val topLevelCode = Expression.CompoundExpression(
                    context.defVar("a", Types.Int),
                    Expression.Copy(
                        2.toExpr(),
                        context.defVar("b", Types.Int)
                    ),
                    context.defVar("c", Types.Int),
                    Expression.FunctionCall(impureFunc),
                    pureFunc,
                    Expression.Return(context.use("c")),
                )

                context.addFunction(pureFunc, emptyList()) { Expression.NoOp }
                context.addFunction(impureFunc, emptyList()) {
                    Expression.CompoundExpression(
                        Expression.Copy(1.toExpr(), context.use("a")),
                    )
                }

                val topLevel = ICScriptTopLevel(context) { topLevelCode }

                val optimized = optimizer.optimizeTopLevel(topLevel)
                optimized.toTreeString() should matchSnapshot
            }

            "remove extra copies expressions" {

                val x = context.defVar("x", Types.Int)
                withData(
                    mapOf(
                        "simple copy" to Expression.CompoundExpression(
                            Expression.Copy(x, x),
                            Expression.Return(Types.Unit.toExpr())
                        ),
                        "copy in return" to Expression.CompoundExpression(
                            Expression.Return(
                                Expression.Copy(x, x)
                            )
                        ),
                        "complex copy" to Expression.CompoundExpression(
                            Expression.Return(
                                Expression.Copy(
                                    Expression.Add(x, 1.toExpr()),
                                    x
                                )
                            )
                        )
                    )
                ) {
                    val optimized = optimizeExpr(it)

                    optimized.toTreeString() should matchSnapshot
                }
            }

            "remove unreachable code" {
                val x = context.defVar("x", Types.Int)
                val expr = Expression.CompoundExpression(
                    Expression.Copy((-4).toExpr(), x),
                    Expression.Loop(
                        Expression.Equals(Expression.Equals(x, 0.toExpr()), 0.toExpr()),
                        Expression.CompoundExpression(
                            Expression.Copy(
                                Expression.Add(x, 1.toExpr()),
                                x
                            )
                        )
                    ),
                    Expression.Return(100.toExpr()),
                    Expression.Return(Expression.Copy(3.toExpr(), x))
                )

                val optimized = optimizeExpr(expr)

                optimized.toTreeString() should matchSnapshot
            }

            "detect useless and infinite loops" {
                val x = context.defVar("x", Types.Int)
                val expr = Expression.CompoundExpression(
                    Expression.Copy(0.toExpr(), x),
                    Expression.Loop(
                        0.toExpr(),
                        Expression.Copy(Expression.Add(x, 4.toExpr()), x)
                    ),
                    Expression.Loop(
                        1.toExpr(),
                        Expression.Copy(Expression.Add(x, 1.toExpr()), x)
                    ),
                    Expression.Copy(7.toExpr(), x),
                    Expression.Return(x)
                )

                val optimized = optimizeExpr(expr)

                optimized.toTreeString() should matchSnapshot
            }
        }
    }
)
