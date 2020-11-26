package com.github.scottbot95.stationeers.ic.dsl

class LineReference(val label: String? = null) : ScriptValue<Number> {
    override val value: Number by lazy {
        lineNum ?: throw IllegalStateException("Cannot access LineReference value before it has been injected")
    }

    private var lineNum: Int? = null

    /**
     * A [Compilable] used to create the label line and specify which line this [LineReference] is pointing to.
     *
     * [inject] must be added exactly once to a single script block in order to use the [LineReference]
     *
     * [inject] will compile to `[label]:` if [label] is set and [CompileOptions.minify] is not set,
     * or an empty result otherwise
     */
    val inject: Compilable = Compilable { context ->
        if (lineNum !== null) {
            throw IllegalStateException("Cannot inject the same reference more than once. Already injected on line $lineNum")
        }
        lineNum = context.startLine
        CompileResults(
            context,
            listOfNotNull(if (label !== null && !context.compileOptions.minify) CompiledLine("$label:") else null)
        )
    }

    override fun toString(context: CompileContext): String {
        return if (label !== null && !context.compileOptions.minify) {
            label
        } else {
            lineNum.toString()
        }
    }
}
