package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.icScript
import com.github.scottbot95.stationeers.ic.highlevel.optimize
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should

class FooTest : WordSpec({
    "script" should {
        "compile correctly" {
            val script = icScript {
                var sum by int()
                var i by int(10)
                loop(i.and(1.toExpr())) {
                    sum += i + i
                    i -= 1.toExpr()
                }
            }.optimize()

            val treeString = script.code.toTreeString()
            println(treeString)
            treeString should matchSnapshot
        }
    }
})
