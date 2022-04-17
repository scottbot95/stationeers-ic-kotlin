package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.Types
import com.github.scottbot95.stationeers.ic.highlevel.defVar
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should

class AssignmentLiftingTest : WordSpec({
    "AssignmentLifting" should {
        "lift pure assignments" {
            val context = ICScriptContext()
            val optimized = AssignmentLifting.optimize(
                Expression.Add(
                    context.defVar("x", Types.Int),
                    3.toExpr(),
                    Expression.Copy(4.toExpr(), context.defVar("Y", Types.Int))
                ),
                context
            )
            optimized.toTreeString() should matchSnapshot
        }

        "lift impure assignments" {
            val context = ICScriptContext()
            val optimized = AssignmentLifting.optimize(
                Expression.Add(
                    context.defVar("x", Types.Int),
                    3.toExpr(),
                    Expression.Copy(
                        Expression.Return(4.toExpr()),
                        context.defVar("Y", Types.Int)
                    )
                ),
                context
            )
            optimized.toTreeString() should matchSnapshot
        }
    }
})