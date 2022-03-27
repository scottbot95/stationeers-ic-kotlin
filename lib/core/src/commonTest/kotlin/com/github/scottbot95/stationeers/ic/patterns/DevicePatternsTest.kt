package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.CompileOptions
import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.util.toScriptValue
import com.github.scottbot95.stationeers.ic.writeToString
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.of

class DevicePatternsTest : WordSpec({
    "waitTillConnected" should {
        "compile to expected code" {
            data class Args(val compileOptions: CompileOptions, val functionCall: Boolean, val expected: String)

            val tests = Exhaustive.of(
                Args(
                    CompileOptions(),
                    false,
                    """
                        WaitTillConnected:
                        yield
                        bdns d0 WaitTillConnected
                        bdns d1 WaitTillConnected
                        bdns d2 WaitTillConnected
                    """.trimIndent()
                ),
                Args(
                    CompileOptions(true),
                    false,
                    """
                        yield
                        bdns d0 0
                        bdns d1 0
                        bdns d2 0
                    """.trimIndent()
                ),
                Args(
                    CompileOptions(),
                    true,
                    """
                        WaitTillConnected:
                        yield
                        bdns d0 WaitTillConnected
                        bdns d1 WaitTillConnected
                        bdns d2 WaitTillConnected
                        j ra
                    """.trimIndent()
                ),
                Args(
                    CompileOptions(true),
                    true,
                    """
                        yield
                        bdns d0 0
                        bdns d1 0
                        bdns d2 0
                        j ra
                    """.trimIndent()
                ),
            )

            checkAll(tests) { args ->
                val devices = listOf(Device.D0, Device.D1, Device.D2)
                    .map { it.toScriptValue() }
                    .toTypedArray()

                val script = ICScriptBuilder.standard().apply {
                    waitTillConnected(*devices, functionCall = args.functionCall)
                }.compile(args.compileOptions)

                val scriptString = script.writeToString()

                scriptString shouldBe args.expected
            }
        }
    }
})
