package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.devices.Light
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// TODO This should probably have a better name. Could be used for generic named container thing
class DeviceDelegateTest {
    @Test
    fun testDeviceDelegateWorks() {
        // FIXME we should probably really do some mocking here once it's available in kotlin multiplatform
        script {
            val d0 by device(::Light)
            val myDeviceName by device(::Light)
            val d2 by device(::Light, name = "OverriddenName")
            val d5 by device(::Light, Device.D5)
            val otherD5 by device(::Light, Device.D5)

            assertEquals(Device.D0, d0.value)
            assertEquals("d0", d0.alias)
            assertEquals(Device.D1, myDeviceName.value)
            assertEquals("myDeviceName", myDeviceName.alias)
            assertEquals(Device.D2, d2.value)
            assertEquals("OverriddenName", d2.alias)
            assertEquals(Device.D5, d5.value)
            assertEquals("d5", d5.alias)
            assertEquals(Device.D5, otherD5.value)
            assertEquals("otherD5", otherD5.alias)

            assertEquals(1, devices.getUsed(Device.D0))
            assertEquals(1, devices.getUsed(Device.D1))
            assertEquals(1, devices.getUsed(Device.D2))
            assertEquals(2, devices.getUsed(Device.D5))
        }
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun testAllDevicesInUse() {
        script {
            Device.values().forEach {
                val testDevice by device(::Light, it, it.name)
            }

            assertFailsWith<IllegalArgumentException> {
                val shouldFail by device(::Light)
            }
        }
    }
}
