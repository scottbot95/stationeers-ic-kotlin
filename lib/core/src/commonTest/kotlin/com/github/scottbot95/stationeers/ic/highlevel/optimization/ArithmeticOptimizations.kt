package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Add
import com.github.scottbot95.stationeers.ic.highlevel.Expression.Negate
import com.github.scottbot95.stationeers.ic.highlevel.Types
import com.github.scottbot95.stationeers.ic.highlevel.defVar
import com.github.scottbot95.stationeers.ic.highlevel.safeUse
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.highlevel.use
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class ArithmeticOptimizations : OptimizationTest(
    body = {
        "Arithmetic Optimizations" should {
            "sum non-zero" {
                val a = context.defVar("a", Types.Int)
                val b = context.defVar("b", Types.Int)
                val c = context.defVar("c", Types.Int)
                val d = context.defVar("d", Types.Int)
                val expr = Add(
                    Add(
                        Add(
                            Add(a, 4.toExpr()),
                            Add(
                                Add(c, d),
                                7.toExpr()
                            )
                        ),
                        Add(a, c)
                    ),
                    Add(b, c)
                )

                val optimized = optimizeExpr(expr)
                optimized.toTreeString() should matchSnapshot
            }

            "sum zero" {
                val a = context.defVar("a", Types.Int)
                val b = context.defVar("b", Types.Int)
                val c = context.defVar("c", Types.Int)
                val d = context.defVar("d", Types.Int)
                val expr = Add(
                    Add(
                        Add(
                            Add(a, 4.toExpr()),
                            Add(
                                Add(c, d),
                                Negate(4.toExpr())
                            )
                        ),
                        Add(a, c)
                    ),
                    Add(b, c)
                )

                val optimized = optimizeExpr(expr)
                optimized.toTreeString() should matchSnapshot
            }

            "sum negations" {
                val expr = Add(
                    Add(
                        Add(
                            Add(
                                Add(
                                    context.defVar("a", Types.Int),
                                    context.defVar("b", Types.Int),
                                ),
                                Negate(context.defVar("c", Types.Int))
                            ),
                            Negate(
                                Add(
                                    context.defVar("d", Types.Int),
                                    Negate(context.defVar("e", Types.Int))
                                )
                            )
                        ),
                        Negate(context.defVar("f", Types.Int))
                    ),
                    Negate(context.defVar("g", Types.Int))
                )

                val optimized = optimizeExpr(expr)
                optimized.toTreeString() should matchSnapshot
            }

            "reduce even nested negations" {
                val expr = Negate(
                    Negate(
                        context.defVar("x", Types.Int)
                    )
                )

                val optimized = optimizeExpr(expr)
                optimized shouldBe context.safeUse("x")
            }

            "reduce odd nested negations" {
                val expr = Negate(
                    Negate(
                        Negate(
                            context.defVar("x", Types.Int)
                        )
                    )
                )

                val optimized = optimizeExpr(expr)
                optimized shouldBe Negate(context.use("x"))
            }

            "extract addition with assignments" {
                val expr = Expression.CompoundExpression(
                    Expression.Return(
                        Add(
                            Add(context.defVar("x", Types.Int), 3.toExpr()),
                            Expression.Copy(4.toExpr(), context.defVar("y", Types.Int))
                        )
                    )
                )

                val optimized = optimizeExpr(expr)
                optimized.toTreeString() shouldBe matchSnapshot
            }
        }
    }
)
