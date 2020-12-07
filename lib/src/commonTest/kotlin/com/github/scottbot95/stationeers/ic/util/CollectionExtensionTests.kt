package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.dsl.PartialCompiledScript
import com.github.scottbot95.stationeers.ic.dsl.compile
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionExtensionTests {

    private val startContext = PartialCompiledScript.empty()

    @Test
    fun testCompileAll() {
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
        assertEquals(expected, results.toString())
    }

    @Test
    fun testCombine() {
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
        assertEquals(expected, results.toString())
    }
}
