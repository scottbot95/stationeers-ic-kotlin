package com.github.scottbot95.stationeers.ic

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.asserter

class MutableLazyTest {

    private var timesRun: Int = 0

    private var testMutableLazy:Int? by mutableLazy { ++timesRun }

    @BeforeTest
    fun setup() {
        timesRun = 0
    }

    @Test
    fun testLazilyCallsInitializerExactlyOnce() {
        asserter.assertEquals(null, 0, timesRun)
        testMutableLazy
        testMutableLazy
        asserter.assertEquals(null, 1, timesRun)
    }

    @Test
    fun testCanChangeValueAfterFirstRead() {
        asserter.assertEquals(null, 1, testMutableLazy)
        testMutableLazy = 10
        asserter.assertEquals(null, 10, testMutableLazy)
    }

    @Test
    fun testCanChangeValueBeforeFirstRead() {
        testMutableLazy = 10
        asserter.assertEquals(null, 10, testMutableLazy)
    }

    @Test
    fun testCanAssignValueToNull() {
        testMutableLazy = null
        asserter.assertEquals(null, null, testMutableLazy)
    }
}