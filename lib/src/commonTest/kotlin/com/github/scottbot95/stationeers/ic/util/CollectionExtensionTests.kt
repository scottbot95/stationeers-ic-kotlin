package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
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

        assertEquals(CompileResults(startContext, "# Hello1", "# Hello2", "# Hello3"), results)
    }
}
