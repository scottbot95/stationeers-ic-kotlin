package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.icScript
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.testUtils.finalizeSnapshots
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should

class FooTest : WordSpec({
    afterSpec {
        finalizeSnapshots(it::class.toDescriptor().id.value)
    }
    "script" should {
        "compile correctly" {
            val script = icScript {
                var sum by int()
                var i by int()
                i = 10.toExpr()
                loop(i) {
                    sum += i
                    i -= 1.toExpr()
                }
            }

            val treeString = script.code.toTreeString()
            println(treeString)
            treeString should matchSnapshot
        }
    }
})
