package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.compileOptions

import com.github.scottbot95.stationeers.ic.testUtils.finalizeSnapshots
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import com.github.scottbot95.stationeers.ic.util.toScriptValue
import com.github.scottbot95.stationeers.ic.writeToString
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.cartesianPairs

class DevicePatternsTest : WordSpec({
    afterSpec {
        finalizeSnapshots(it::class.toDescriptor().id.value)
    }
    "waitTillConnected" should {
        "compile to expected code" {
            val gen = Exhaustive.compileOptions().cartesianPairs(Exhaustive.boolean())
            checkAll(gen) { (compileOptions, functionCall) ->
                val devices = listOf(Device.D0, Device.D1, Device.D2)
                    .map { it.toScriptValue() }
                    .toTypedArray()

                val script = ICScriptBuilder.standard().apply {
                    waitTillConnected(*devices, functionCall = functionCall)
                }.compile(compileOptions)

                val scriptString = script.writeToString()

                scriptString should matchSnapshot
            }
        }
    }
})
