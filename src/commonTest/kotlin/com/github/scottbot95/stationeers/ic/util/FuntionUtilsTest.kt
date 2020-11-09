package com.github.scottbot95.stationeers.ic.util

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.asserter

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
        asserter.assertEquals(null, 1, result)
    }

    @Test
    fun testOnlyCallsOnce() {
        wrappedFunc()
        wrappedFunc()
        wrappedFunc()
        asserter.assertEquals(null, 1, timesCalled)
    }
}