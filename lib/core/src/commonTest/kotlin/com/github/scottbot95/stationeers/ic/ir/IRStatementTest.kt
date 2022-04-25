package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel
import com.github.scottbot95.stationeers.ic.highlevel.Types
import com.github.scottbot95.stationeers.ic.highlevel.defVar
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class IRStatementTest : WordSpec({
    "IRStatement" should {
        "track previous statements correctly" {
            val context = ICScriptContext()
            val topLevel = ICScriptTopLevel(context) {
                val x = context.defVar("x", Types.Int)
                Expression.CompoundExpression(
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
            }

            val compilation = topLevel.compile()
            compilation.forEach { statement ->
                statement.prev.forEach {
                    (it.next == statement || (it as IRStatement.ConditionalStatement).cond == statement) shouldBe true
                }
                println("Prev correct for `$statement`")
            }
        }
    }
})
