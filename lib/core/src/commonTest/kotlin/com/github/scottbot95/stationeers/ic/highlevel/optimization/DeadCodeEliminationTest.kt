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
import io.kotest.matchers.should

class DeadCodeEliminationTest : OptimizationTest(
    body = {
        "DeadCodeElimination" should {
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

            "optimizes correctly" {
                optimized.toTreeString() should matchSnapshot
            }
        }
    }
)
