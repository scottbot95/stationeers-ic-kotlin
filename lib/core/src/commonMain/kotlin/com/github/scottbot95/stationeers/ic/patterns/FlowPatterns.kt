package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.CodeBlock
import com.github.scottbot95.stationeers.ic.CodeBlockBuilder
import com.github.scottbot95.stationeers.ic.LineReference
import com.github.scottbot95.stationeers.ic.instructions.Flow
import com.github.scottbot95.stationeers.ic.instructions.Flow.Conditional

private fun conditionalJump(target: LineReference, conditional: Conditional?) = if (conditional != null) {
    Flow.Branch(conditional, target)
} else {
    Flow.Jump(target)
}

fun <T : CodeBlockBuilder<T>> T.loop(
    block: CodeBlock,
    conditional: Conditional? = null,
    label: String? = null,
    atLeastOnce: Boolean = false,
): T {
    val start = newLineReference(label)
    val end = newLineReference(label?.let { "${it}_end" })

    appendEntry(start.mark)

    if (!atLeastOnce && conditional != null) {
        appendEntry(Flow.Branch(conditional, end))
    }
    appendEntry(block)
    if (atLeastOnce) {
        appendEntry(conditionalJump(start, conditional))
    } else {
        appendEntry(Flow.Jump(start))
    }
    if (!atLeastOnce && conditional != null) {
        appendEntry(end.mark)
    }

    return this
}

fun <T : CodeBlockBuilder<T>> T.forever(
    block: CodeBlock,
    label: String = "start",
) = loop(block, label = label)

data class ConditionalBlock(
    val condition: Conditional,
    val codeBlock: CodeBlock,
    val label: String? = null
)

/**
 * Creates a construct similar to a switch statement. Evaluates
 * the [Conditional] of each case in order, executing the [CodeBlock] of
 * the first passing [ConditionalBlock] in [cases].
 *
 * @param cases The series of cases, in order
 * @param default The default [CodeBlock] to execute when no case of [cases] passes
 * @param fallThrough Whether to enable fall through to subsequent [cases] after the first passing case
 * @param labelPrefix String to prefix before all labels of this cond
 */
fun <T : CodeBlockBuilder<T>> T.cond(
    vararg cases: ConditionalBlock,
    default: CodeBlock? = null,
    fallThrough: Boolean = false,
    labelPrefix: String? = null,
) {
    require(cases.isNotEmpty()) { "cond requires at least one case" }

    val endRef = newLineReference((labelPrefix ?: "cond") + "End")
    val defaultRef = default?.let { newLineReference((labelPrefix ?: "cond") + "Default") }

    // Create labels for all cases
    val labels = cases.map { case ->
        newLineReference(case.label?.let { labelPrefix.orEmpty() + it })
    }

    // Append case checks
    cases.forEachIndexed { i, case ->
        appendEntry(Flow.Branch(case.condition, labels[i]))
    }
    if (defaultRef != null) {
        appendEntry(Flow.Jump(defaultRef))
    }

    // Append case code blocks
    cases.forEachIndexed { i, case ->
        // Inject reference to start of this case
        appendEntry(labels[i].mark)
        // jump to start of next case if condition doesn't match
        appendEntry(case.codeBlock)
        // Jump to end if not in fallthrough mode
        if (!fallThrough) appendEntry(Flow.Jump(endRef))
    }
    if (defaultRef != null) {
        appendEntry(defaultRef.mark)
        appendEntry(default)
    }

    appendEntry(endRef.mark)
}
