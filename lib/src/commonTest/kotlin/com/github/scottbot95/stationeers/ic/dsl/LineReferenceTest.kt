package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.Statement
import kotlin.test.Test
import kotlin.test.assertEquals

class LineReferenceTest {
    @Test
    fun testInjectsCorrectLine() {
        val reference = FixedLineReference()
        script {
            spacing(5, true)
            +reference.inject
        }.compile()

        assertEquals(5, reference.lineNum)
    }

    @Test
    fun testInjectsLabel() {
        val reference = FixedLineReference("testLabel")
        val script = script {
            spacing(5, true)
            +reference.inject
        }.compile()

        assertEquals("testLabel:", script.toString().split("\n").last())
    }

    @Test
    fun testRelativeReference() {
        val reference = FixedLineReference("testLabel")
        val script = script {
            spacing(5, true)
            +reference.inject
            +Compilable { it + CompiledOperation(ScriptValue.of(reference.toRelative()), statement = Statement.Noop) }
        }.compile()

        assertEquals("-1", script.toString().split("\n").last())
    }

    @Test
    fun testToRelativeToFixed() {
        val reference = FixedLineReference("testLabel")
        val script = script {
            spacing(5, true)
            +reference.inject
            +Compilable {
                it + CompiledOperation(
                    ScriptValue.of(reference.toRelative().toFixed().value.toString()),
                    statement = Statement.Noop
                )
            }
        }.compile()

        assertEquals("5", script.toString().split("\n").last())
    }
}
