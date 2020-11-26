package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.JumpTarget
import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.util.Conditional

/**
 * [ScriptBlock] that will wrap contents in a loop, optionally with a condition
 * If [label] is specified and [CompileOptions.minify] is not set, a label will be created at the start of the loop
 * If [atLeastOnce] is true, the condition will be checked after each iteration ensuring that the loop will be run
 * at least once
 */
class LoopingScriptBlock(
    private val label: String? = null,
    private val conditional: Conditional? = null,
    private val atLeastOnce: Boolean = true,
    private val shouldYield: Boolean = true,
    scope: ScriptBlock? = null
) : SimpleScriptBlock(scope) {

    private class LoopLabel(private val loop: LoopingScriptBlock) : ScriptValue<String> {
        override val value: String = loop.label ?: ""

        override fun toString(context: CompileContext): String {
            return if (value.isEmpty() || context.compileOptions.minify) {
                loop.startLine?.toString() ?: throw IllegalStateException("loopStart can only be used inside it's own loop")
            } else {
                value
            }
        }
    }

    // TODO not really a fan, maybe need to rework compile process
    val loopStart: ScriptValue<String> = LoopLabel(this)

    private var startLine: Int? = null

    override fun compile(context: CompileContext): CompileResults {
        startLine = context.startLine

        val labelLine = when {
            label !== null && !context.compileOptions.minify -> listOf("$label:")
            else -> listOf()
        }

        val yieldLine = if (shouldYield) listOf("yield") else listOf()

        val results = if (conditional !== null && !atLeastOnce) {
            val innerResults = super.compile(context + labelLine.size)
            val loopLines = labelLine.size + yieldLine.size + innerResults.lines.size + 2
            val branchOp = Operation.Branch(conditional, JumpTarget.Line(context.startLine + loopLines)).compile(context)
            val prefix = labelLine + branchOp.lines + yieldLine
            val suffix = Operation.Jump(loopStart).compile(context + prefix.size + innerResults.lines.size)

            innerResults.withLines(prefix + innerResults.lines + suffix.lines)
        } else {
            val operation = if (conditional !== null) {
                Operation.Branch(conditional, loopStart)
            } else {
                Operation.Jump(loopStart)
            }
            val prefix = labelLine + yieldLine
            val innerResults = super.compile(context + prefix.size)
            val suffix = operation.compile(context + prefix.size + innerResults.lines.size)

            innerResults.withLines(prefix + innerResults.lines + suffix.lines)
        }

        startLine = null

        return results
    }
}
