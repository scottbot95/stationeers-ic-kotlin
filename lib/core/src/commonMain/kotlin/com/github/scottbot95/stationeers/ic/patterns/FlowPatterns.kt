package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.CodeBlock
import com.github.scottbot95.stationeers.ic.CodeBlockBuilder
import com.github.scottbot95.stationeers.ic.LineReference
import com.github.scottbot95.stationeers.ic.instructions.Flow

private fun conditionalJump(target: LineReference, conditional: Flow.Conditional?) = if (conditional != null) {
    Flow.Branch(conditional, target)
} else {
    Flow.Jump(target)
}

fun <T : CodeBlockBuilder<T>> T.loop(
    block: CodeBlock,
    conditional: Flow.Conditional? = null,
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