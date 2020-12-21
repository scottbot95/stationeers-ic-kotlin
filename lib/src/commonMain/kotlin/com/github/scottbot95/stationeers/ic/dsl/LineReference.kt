package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.Statement

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
    override val value: Int
        get() = checkLinePresent(lineNum)
    override var lineNum: Int? = null
        private set

    override val inject: Compilable = Compilable { partial ->
        if (lineNum !== null) {
            throw IllegalStateException("Cannot inject the same reference more than once. Already injected on line $lineNum")
        }
        lineNum = partial.nextLine

        if (label != null && !partial.compileOptions.minify) {
            partial + CompiledOperation(ScriptValue.of("$label:"), statement = Statement.Noop)
        } else {
            partial
        }
    }

    override fun toString(context: CompileContext) = lineNum.toString()
}

class OffsetLineReference<out T : LineReference> internal constructor(
    private val reference: T,
    private val offset: Int
) : LineReference by reference {
    override val lineNum: Int? get() = reference.lineNum?.plus(offset)
}

class FixedLineReference internal constructor(
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

class RelativeLineReference internal constructor(
    override val label: String? = null,
    reference: LineReference = InjectableReference(label)
) : LineReference by reference {
    override fun toString(context: CompileContext): String = (value - context.startLine).toString()
}

fun LineReference.toRelative() = RelativeLineReference(label, this)
fun LineReference.toFixed() = FixedLineReference(label, this)
fun <T : LineReference> T.offset(offset: Int) = OffsetLineReference(this, offset)

private fun checkLinePresent(lineNum: Int?) =
    lineNum ?: throw IllegalStateException("Cannot access value of LineReference value before it has been injected")
