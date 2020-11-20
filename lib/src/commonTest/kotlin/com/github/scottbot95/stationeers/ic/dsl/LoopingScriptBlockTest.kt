package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.util.GreaterThan
import kotlin.test.Test
import kotlin.test.assertEquals

class LoopingScriptBlockTest {

    @Test
    fun testInfiniteLoopWithLabel() {
        val loopingBlock = LoopingScriptBlock("loop").apply {
            comment("Something inside the block")
            comment("Should probably have something more complex here")
        }

        val options = CompileOptions()
        val context = CompileContext(5)

        val expected =
            """
            loop:
            yield
            # Something inside the block
            # Should probably have something more complex here
            j loop
            """.trimIndent()
        val results = loopingBlock.compile(options, context)
        assertEquals(expected, results.asString)
    }

    @Test
    fun testInfiniteLoopWithoutLabel() {
        val loopingBlock = LoopingScriptBlock().apply {
            comment("Something inside the block")
            comment("Should probably have something more complex here")
        }

        val options = CompileOptions()
        val context = CompileContext(5)

        val expected =
            """
            yield
            # Something inside the block
            # Should probably have something more complex here
            j 5
            """.trimIndent()
        val results = loopingBlock.compile(options, context)
        assertEquals(expected, results.asString)
    }

    @Test
    fun testConditionalLoopWithLabel() {
        val loopingBlock = LoopingScriptBlock(
            "loop",
            GreaterThan(ScriptValue.of(2), ScriptValue.of(1))
        ).apply {
            comment("Something inside the block")
            comment("Should probably have something more complex here")
        }

        val options = CompileOptions()
        val context = CompileContext(5)

        val expected =
            """
            loop:
            yield
            # Something inside the block
            # Should probably have something more complex here
            bgt 2 1 loop
            """.trimIndent()
        val results = loopingBlock.compile(options, context)
        assertEquals(expected, results.asString)
    }

    @Test
    fun testConditionalLoopWithoutLabel() {
        val loopingBlock = LoopingScriptBlock(
            null,
            GreaterThan(ScriptValue.of(2), ScriptValue.of(1))
        ).apply {
            comment("Something inside the block")
            comment("Should probably have something more complex here")
        }

        val options = CompileOptions()
        val context = CompileContext(5)

        val expected =
            """
            yield
            # Something inside the block
            # Should probably have something more complex here
            bgt 2 1 5
            """.trimIndent()
        val results = loopingBlock.compile(options, context)
        assertEquals(expected, results.asString)
    }

    @Test
    fun testConditionalAtStartLoopWithLabel() {
        val loopingBlock = LoopingScriptBlock(
            "loop",
            GreaterThan(ScriptValue.of(2), ScriptValue.of(1)),
            false
        ).apply {
            comment("Something inside the block")
            comment("Should probably have something more complex here")
        }

        val options = CompileOptions()
        val context = CompileContext(5)

        val expected =
            """
            loop:
            bgt 2 1 11
            yield
            # Something inside the block
            # Should probably have something more complex here
            j loop
            """.trimIndent()

        val results = loopingBlock.compile(options, context)

        assertEquals(expected, results.asString)
    }

    @Test
    fun testConditionalAtStartLoopWithoutLabel() {
        val loopingBlock = LoopingScriptBlock(
            null,
            GreaterThan(ScriptValue.of(2), ScriptValue.of(1)),
            false
        ).apply {
            comment("Something inside the block")
            comment("Should probably have something more complex here")
        }

        val options = CompileOptions()
        val context = CompileContext(5)

        val expected =
            """
            bgt 2 1 10
            yield
            # Something inside the block
            # Should probably have something more complex here
            j 5
            """.trimIndent()

        val results = loopingBlock.compile(options, context)

        assertEquals(expected, results.asString)
    }
}
