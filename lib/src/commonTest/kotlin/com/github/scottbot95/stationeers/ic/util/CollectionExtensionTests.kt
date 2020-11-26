package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionExtensionTests {

    @Test
    fun testCompileAll() {
        val startContext = CompileContext(startLine = 5)
        val results = listOf(
            Operation.Comment("Hello1"),
            Operation.Comment("Hello2"),
            Operation.Comment("Hello3"),
        ).compileAll(startContext)

        val expected =
            """
            # Hello1
            # Hello2
            # Hello3
            """.trimIndent()
        assertEquals(expected, results.asString)
    }

    @Test
    fun testCombine() {
        val startContext = CompileContext(startLine = 5)
        val results = listOf(
            Operation.Comment("Hello1"),
            Operation.Comment("Hello2"),
            Operation.Comment("Hello3"),
        ).combine().compile(startContext)

        val expected =
            """
            # Hello1
            # Hello2
            # Hello3
            """.trimIndent()
        assertEquals(expected, results.asString)
    }
}
