package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.CompileOptions
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.LineReference
import com.github.scottbot95.stationeers.ic.instructions.Flow
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.writeToString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class ScriptValueTest : WordSpec({
    "LineReference" should {
        "throw if not injected" {
            val script = ICScriptBuilder.standard()
                .appendEntry(Flow.Jump(LineReference()))
                .compile(CompileOptions())
            val ex = shouldThrow<IllegalStateException> {
                script.writeToString()
            }

            ex shouldHaveMessage "LineReference mark was not compiled"
        }

        "throw if injected more than once" {
            val lineRef = LineReference()
            val scriptBuilder = ICScriptBuilder.standard()
                .appendEntry(lineRef.mark)
                .appendEntry(lineRef.mark)

            val ex = shouldThrow<IllegalStateException> {
                scriptBuilder.compile(CompileOptions())
            }

            ex shouldHaveMessage "Cannot compile this LineReference marker in more than one place"
        }

        "render label properly" {
            val lineRef = LineReference("MyLabel")
            val script = ICScriptBuilder.standard()
                .appendEntry(lineRef.mark)
                .appendEntry(Flow.Jump(lineRef))
                .compile(CompileOptions())

            script.writeToString() shouldBe """
                MyLabel:
                j MyLabel
            """.trimIndent()
        }

        "render references properly before mark" {
            val lineRef = LineReference()
            val script = ICScriptBuilder.standard()
                .appendEntry(Flow.Jump(lineRef))
                .appendEntry(Misc.Comment("Just some filler here"))
                .appendEntry(Misc.Comment("Some more filler for fun!"))
                .appendEntry(lineRef.mark)
                .compile(CompileOptions())

            script.writeToString() shouldBe """
                j 3
                # Just some filler here
                # Some more filler for fun!
            """.trimIndent()
        }
    }
})
