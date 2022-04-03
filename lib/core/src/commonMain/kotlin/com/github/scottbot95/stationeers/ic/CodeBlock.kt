package com.github.scottbot95.stationeers.ic

interface CodeBlockBuilder<out T : CodeBlockBuilder<T>> {
    fun appendEntry(entry: Compilable): T

    /**
     * Create a new [LineReference] based of the requested [label]. Should automatically rename labels to avoid conflicts
     *
     * __NOTE:__ TODO The default implementation does __NOT__ dedupe names
     */
    fun newLineReference(label: String? = null) = LineReference(label)
}

interface CodeBlock : CodeBlockBuilder<CodeBlock>, Compilable

fun <T : CodeBlockBuilder<T>> T.appendLineReference(label: String? = null) =
    newLineReference(label).also { appendEntry(it.mark) }
