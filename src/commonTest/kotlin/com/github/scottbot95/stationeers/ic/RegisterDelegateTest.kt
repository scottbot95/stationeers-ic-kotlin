package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.register
import com.github.scottbot95.stationeers.ic.dsl.script
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// TODO This should probably have a better name. Could be used for generic named container thing
class RegisterDelegateTest {
    @Test
    fun testRegisterDelegateWorks() {
        // FIXME we should probably really do some mocking here once it's available in kotlin multiplatform
        script {
            val r0 by register
            val myRegisterName by register
            val r2 by register(name = "OverriddenName")
            val r14 by register(Register.R14)
            val otherR14 by register(Register.R14)

            assertEquals(Register.R0, r0.value.register)
            assertEquals("r0", r0.alias)
            assertEquals(Register.R1, myRegisterName.value.register)
            assertEquals("myRegisterName", myRegisterName.alias)
            assertEquals(Register.R2, r2.value.register)
            assertEquals("OverriddenName", r2.alias)
            assertEquals(Register.R14, r14.value.register)
            assertEquals("r14", r14.alias)
            assertEquals(Register.R14, otherR14.value.register)
            assertEquals("otherR14", otherR14.alias)
        }
    }

    @Test
    @Suppress("UNUSED_VARIABLE")
    fun testAllRegistersInUse() {
        script {
            Register.values().forEach {
                val testRegister by register(name = it.name)
            }

            assertFailsWith<IllegalArgumentException> {
                val shouldFail by register
            }
        }
    }
}
