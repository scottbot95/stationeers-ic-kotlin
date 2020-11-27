package com.github.scottbot95.stationeers.ic.dsl

interface LineReference : ScriptValue<Int> {
    /**
     * A [Compilable] used to create the label line and specify which line this [LineReference] is pointing to.
     *
     * [inject] must be added exactly once to a single script block in order to use the [LineReference]
     *
     * [inject] will compile to `[label]:` if [label] is set and [CompileOptions.minify] is not set,
     * or an empty result otherwise
     */
    val inject: Compilable

    val lineNum: Int?
    val label: String?
}

private class InjectableReference(override val label: String? = null) : LineReference {
    override val value: Int by lazy {
        lineNum ?: throw IllegalStateException("Cannot access LineReference value before it has been injected")
    }

    override var lineNum: Int? = null
        protected set

    override val inject: Compilable = Compilable { context ->
        if (lineNum !== null) {
            throw IllegalStateException("Cannot inject the same reference more than once. Already injected on line $lineNum")
        }
        lineNum = context.startLine
        CompileResults(
            context,
            listOfNotNull(if (label !== null && !context.compileOptions.minify) CompiledLine("$label:") else null)
        )
    }

    override fun toString(context: CompileContext) = lineNum.toString()
}

class FixedLineReference(
    override val label: String? = null,
    reference: LineReference = InjectableReference(label)
) : LineReference by reference {
    override fun toString(context: CompileContext): String {
        return if (label !== null && !context.compileOptions.minify) {
            label
        } else {
            lineNum.toString()
        }
    }
}

class RelativeLineReference(
    override val label: String? = null,
    reference: LineReference = InjectableReference(label)
) : LineReference by reference {
    override fun toString(context: CompileContext): String = (value - context.startLine).toString()
}

fun LineReference.toRelative() = RelativeLineReference(label, this)
fun LineReference.toFixed() = FixedLineReference(label, this)
