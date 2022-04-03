package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Flow
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.testUtils.finalizeSnapshots
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class ScriptValueTest : WordSpec({
    afterSpec {
        finalizeSnapshots(it::class.toDescriptor().id.value)
    }
    "LineReference" should {
        "throw if not injected" {
            val script = ICScriptBuilder.standard()
                .appendEntry(Flow.Jump(LineReference()))
                .compile(CompileOptions())
            val ex = shouldThrow<IllegalStateException> {
                script.writeToString()
            }

            ex shouldHaveMessage "Mark for LineReference(label=null) was not compiled"
        }

        "throw if injected more than once" {
            val lineRef = LineReference()
            val scriptBuilder = ICScriptBuilder.standard()
                .appendEntry(lineRef.mark)
                .appendEntry(lineRef.mark)

            val ex = shouldThrow<IllegalStateException> {
                scriptBuilder.compile(CompileOptions())
            }

            ex shouldHaveMessage "Cannot compile marker for LineReference(label=null) in more than once"
        }

        "render label properly" {
            val lineRef = LineReference("MyLabel")
            val script = ICScriptBuilder.standard()
                .appendEntry(lineRef.mark)
                .appendEntry(Flow.Jump(lineRef))
                .compile(CompileOptions())

            script.writeToString() shouldBe matchSnapshot
        }

        "render references properly before mark" {
            val lineRef = LineReference()
            val script = ICScriptBuilder.standard()
                .appendEntry(Flow.Jump(lineRef))
                .appendEntry(Misc.Comment("Just some filler here"))
                .appendEntry(Misc.Comment("Some more filler for fun!"))
                .appendEntry(lineRef.mark)
                .compile(CompileOptions())

//            script.writeToString() shouldBe """
//                j 3
//                # Just some filler here
//                # Some more filler for fun!
//            """.trimIndent()

            script.writeToString() should matchSnapshot
        }
    }
})
