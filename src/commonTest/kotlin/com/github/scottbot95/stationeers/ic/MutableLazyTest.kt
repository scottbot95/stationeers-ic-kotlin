package com.github.scottbot95.stationeers.ic

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MutableLazyTest {

    private var timesRun: Int = 0

    private var testMutableLazy: Int? by mutableLazy { ++timesRun }

    @BeforeTest
    fun setup() {
        timesRun = 0
    }

    @Test
    fun testLazilyCallsInitializerExactlyOnce() {
        assertEquals(0, timesRun)
        testMutableLazy
        testMutableLazy
        assertEquals(1, timesRun)
    }

    @Test
    fun testCanChangeValueAfterFirstRead() {
        assertEquals(1, testMutableLazy)
        testMutableLazy = 10
        assertEquals(10, testMutableLazy)
    }

    @Test
    fun testCanChangeValueBeforeFirstRead() {
        testMutableLazy = 10
        assertEquals(10, testMutableLazy)
    }

    @Test
    fun testCanAssignValueToNull() {
        testMutableLazy = null
        assertEquals(null, testMutableLazy)
    }
}
