package com.github.scottbot95.stationeers.ic.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class CompilableTests {
    @Test
    fun testCompileResultsPlus() {
        val a = CompileResults(CompileContext(startLine = 5), "Line1")
        val b = CompileResults(CompileContext(startLine = 6), "Line2")
        val combined = a + b

        assertEquals(CompileResults(a.startContext, "Line1", "Line2"), combined)
    }
}
