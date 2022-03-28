package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.CompileOptions
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.instructions.Flow
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.util.toScriptValue
import com.github.scottbot95.stationeers.ic.writeToString
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class FlowPatternsTest : WordSpec({
    "forever" should {
        "produce expected output" {
            val builder = ICScriptBuilder.standard()
            val block = builder.newCodeBlock()
                .appendEntry(Misc.Comment("Inside the loop"))
            builder.forever(block, "myLabel")

            val script = builder.compile(CompileOptions())
            val scriptString = script.writeToString()

            scriptString shouldBe """
                myLabel:
                # Inside the loop
                j myLabel
            """.trimIndent()

        }
    }

    "loop" should {
        // TODO add more tests here
        "produce expected output" {
            val script = ICScriptBuilder.standard().apply {
                val block = newCodeBlock()
                    .appendEntry(Misc.Comment("I'm inside the loop!"))
                loop(block, Flow.Conditional.LessThan(0.toScriptValue(), 1.toScriptValue()), "myLoop")
            }.compile(CompileOptions())

            val scriptString = script.writeToString()

            scriptString shouldBe """
                myLoop:
                blt 0 1 myLoop_end
                # I'm inside the loop!
                j myLoop
                myLoop_end:
            """.trimIndent()
        }
    }
})