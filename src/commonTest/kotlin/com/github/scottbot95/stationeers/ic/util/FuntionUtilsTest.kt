package com.github.scottbot95.stationeers.ic.util

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FuntionUtilsTest {
    var timesCalled = 0

    val wrappedFunc = once { ++timesCalled }

    @BeforeTest
    fun setup() {
        timesCalled = 0
    }

    @Test
    fun testCanCallThrough() {
        val result = wrappedFunc()
        assertEquals(1, result)
    }

    @Test
    fun testOnlyCallsOnce() {
        wrappedFunc()
        wrappedFunc()
        wrappedFunc()
        assertEquals(1, timesCalled)
    }
}
