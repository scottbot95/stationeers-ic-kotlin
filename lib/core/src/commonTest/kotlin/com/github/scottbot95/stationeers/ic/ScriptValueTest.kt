package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Flow
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.testUtils.finalizeSnapshots
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll

class ScriptValueTest : WordSpec({
    afterSpec {
        finalizeSnapshots(it::class.toDescriptor().id.value)
    }
    "LineReference" should {
        "throw if not injected" {
            checkAll(Exhaustive.compileOptions()) { options ->
                val script = ICScriptBuilder.standard()
                    .appendEntry(Flow.Jump(LineReference()))
                    .compile(options)
                val ex = shouldThrow<IllegalStateException> {
                    script.writeToString()
                }

                ex shouldHaveMessage "Mark for LineReference(label=null) was not compiled"
            }
        }

        "throw if injected more than once" {
            checkAll(Exhaustive.compileOptions()) { options ->
                val lineRef = LineReference()
                val scriptBuilder = ICScriptBuilder.standard()
                    .appendEntry(lineRef.mark)
                    .appendEntry(lineRef.mark)

                val ex = shouldThrow<IllegalStateException> {
                    scriptBuilder.compile(options)
                }

                ex shouldHaveMessage "Cannot compile marker for LineReference(label=null) in more than once"
            }
        }

        "render label properly" {
            checkAll(Exhaustive.compileOptions()) { options ->
                val lineRef = LineReference("MyLabel")
                val script = ICScriptBuilder.standard()
                    .appendEntry(Misc.Comment("Some filler"))
                    .appendEntry(lineRef.mark)
                    .appendEntry(Flow.Jump(lineRef))
                    .compile(options)

                script.writeToString() should matchSnapshot
            }
        }

        "render references properly before mark" {
            checkAll(Exhaustive.compileOptions()) { options ->
                val lineRef = LineReference()
                val script = ICScriptBuilder.standard()
                    .appendEntry(Flow.Jump(lineRef))
                    .appendEntry(Misc.Comment("Just some filler here"))
                    .appendEntry(Misc.Comment("Some more filler for fun!"))
                    .appendEntry(lineRef.mark)
                    .compile(options)

                script.writeToString() should matchSnapshot
            }
        }
    }
})
