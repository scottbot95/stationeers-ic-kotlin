package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.devices.Light
import com.github.scottbot95.stationeers.ic.devices.On
import com.github.scottbot95.stationeers.ic.devices.Open
import com.github.scottbot95.stationeers.ic.devices.Switch
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScriptBlockTest {

    private lateinit var testScript: ScriptBlock

    @BeforeTest
    fun setup() {
        testScript = script {
            val lightSwitch by device(::Switch, Device.D0)
            val light by device(::Light, Device.D1, "Light")
            val loopCount by register

            comment("Loop forever")
            forever("loop") {
                val switchSetting = readDevice(lightSwitch.Open)
                writeDevice(light.On, switchSetting)
                inc(loopCount)
            }

            +"# You can add text directly as well"
        }
    }

    @Test
    fun testCompileSettings() {
        val expected =
            """
            alias dlightSwitch d0
            alias dLight d1
            alias rloopCount r0
            # Loop forever
            loop:
            yield
            l r1 lightSwitch Open
            s Light On r1
            add loopCount loopCount 1
            j loop
            # You can add text directly as well
            """.trimIndent()

        val results = testScript.compile(CompileOptions(), CompileContext())

        assertEquals(expected, results.asString)
    }

    @Test
    fun testCompileMinify() {
        val expected =
            """
            yield
            l r1 d0 Open
            s d1 On r1
            add r0 r0 1
            j 0
            # You can add text directly as well
            """.trimIndent()

        val results = testScript.compile {
            minify = true
        }

        assertEquals(expected, results.asString)
    }
}
