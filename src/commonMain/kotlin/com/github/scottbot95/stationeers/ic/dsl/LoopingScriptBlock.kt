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
    private val atLeastOnce: Boolean = true
) : SimpleScriptBlock() {
    override fun compile(options: CompileOptions, context: CompileContext): CompileResults {
        val (labelLine: String?, loopStart: JumpTarget<*>) = if (label !== null && !options.minify) {
            "$label:" to JumpTarget.Label(label)
        } else {
            null to JumpTarget.Line(context.startLine)
        }

        return if (conditional !== null && !atLeastOnce) {
            val labelLines = listOfNotNull(labelLine)
            val innerResults = super.compile(options, context + labelLines.size)
            val loopLines = labelLines.size + innerResults.lines.size + 2
            val branchOp = Operation.Branch(conditional, JumpTarget.Line(context.startLine + loopLines)).compile(options, context)
            val prefix = labelLines + branchOp.lines
            val suffix = Operation.Jump(loopStart).compile(options, context + prefix.size + innerResults.lines.size)

            innerResults.withLines(prefix + innerResults.lines + suffix.lines)
        } else {
            val operation = if (conditional !== null) {
                Operation.Branch(conditional, loopStart)
            } else {
                Operation.Jump(loopStart)
            }
            val prefix = listOfNotNull(labelLine)
            val innerResults = super.compile(options, context + prefix.size)
            val suffix = operation.compile(options, context + prefix.size + innerResults.lines.size)

            innerResults.withLines(prefix + innerResults.lines + suffix.lines)
        }
    }
}
