package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.compileOptions
import com.github.scottbot95.stationeers.ic.instructions.Flow
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.testUtils.finalizeSnapshots
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toScriptValue
import com.github.scottbot95.stationeers.ic.writeToString
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll

class FlowPatternsTest : WordSpec({
    afterSpec {
        finalizeSnapshots(it::class.toDescriptor().id.value)
    }
    "forever" should {
        "produce expected output" {
            checkAll(Exhaustive.compileOptions()) { options ->

                val builder = ICScriptBuilder.standard()
                val block = builder.newCodeBlock()
                    .appendEntry(Misc.Comment("Inside the loop"))
                builder.forever(block, "myLabel")

                val script = builder.compile(options)
                val scriptString = script.writeToString()

                scriptString should matchSnapshot
            }
        }
    }

    "loop" should {
        // TODO add more tests here
        "produce expected output" {
            checkAll(Exhaustive.compileOptions()) { options ->

                val script = ICScriptBuilder.standard().apply {
                    val block = newCodeBlock()
                        .appendEntry(Misc.Comment("I'm inside the loop!"))
                    loop(block, Flow.Conditional.LessThan(0.toScriptValue(), 1.toScriptValue()), "myLoop")
                }.compile(options)

                val scriptString = script.writeToString()

                scriptString should matchSnapshot
            }
        }
    }

    "cond" should {
        "produce expected output" {
            checkAll(Exhaustive.compileOptions()) { options ->
                val script = ICScriptBuilder.standard().apply {
                    cond(
                        ConditionalBlock(
                            Flow.Conditional.EqualToZero(1.toScriptValue()),
                            newCodeBlock().appendEntry(Misc.Comment("Block 1")),
                            "case1"
                        ),
                        ConditionalBlock(
                            Flow.Conditional.EqualToZero(2.toScriptValue()),
                            newCodeBlock().appendEntry(Misc.Comment("Block 2")),
                            "case2"
                        ),
                        ConditionalBlock(
                            Flow.Conditional.EqualToZero(3.toScriptValue()),
                            newCodeBlock().appendEntry(Misc.Comment("Block 3")),
                            "case3"
                        ),
                        default = newCodeBlock().appendEntry(Misc.Comment("default")),
                        labelPrefix = "myCond",
                    )
                }.compile(options)

                val scriptString = script.writeToString()

                scriptString should matchSnapshot
            }
        }
    }
})
