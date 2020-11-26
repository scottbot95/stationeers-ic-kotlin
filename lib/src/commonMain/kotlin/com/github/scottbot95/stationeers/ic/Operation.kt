package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.of
import com.github.scottbot95.stationeers.ic.util.Conditional
import com.github.scottbot95.stationeers.ic.util.FlagEnum

sealed class JumpTarget<T : Any>(override val value: T) : ScriptValue<T> {
    data class Line(val line: Int) : JumpTarget<Int>(line)

    // TODO Make labels have their own boxed type?
    data class Label(val label: String) : JumpTarget<String>(label)
}

enum class JumpType : FlagEnum {
    FUNCTION,
    RELATIVE,
}

/**
 * Generic class representing an operation within the MIPS language
 */
sealed class Operation : Compilable {

    abstract val args: Array<out ScriptValue<*>>
    abstract val opCode: String

    override fun compile(context: CompileContext): CompileResults {
        val combinedArgs = args.joinToString(" ") { it.toString(context) }
        return CompileResults(context, lines = listOf("$opCode $combinedArgs"))
    }

    open class SimpleOperation internal constructor(
        override val opCode: String,
        override vararg val args: ScriptValue<*>
    ) : Operation()

    //region IO Operations

    data class Load(val output: ScriptValue<Register>, val device: ScriptValue<Device>, val deviceVar: String) :
        SimpleOperation("l", output, device, ScriptValue.of(deviceVar))

    data class Save(val device: ScriptValue<Device>, val deviceVar: String, val value: ScriptValue<*>) :
        SimpleOperation("s", device, ScriptValue.of(deviceVar), value)

    data class Move(val output: ScriptValue<Register>, val value: ScriptValue<*>) :
        SimpleOperation("move", output, value)

    //endregion

    //region Math operations

    data class Add(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("add", output, a, b)

    data class Subtract(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("sub", output, a, b)

    //endregion

    //region Control flow operations

    // TODO refactor options into a JumpOptions data class
    data class Jump(val target: ScriptValue<*>, val type: JumpType? = null) : Operation() {
        override val opCode: String = when (type) {
            JumpType.FUNCTION -> "jal"
            JumpType.RELATIVE -> "jr"
            null -> "j"
        }
        override val args: Array<out ScriptValue<*>> = arrayOf(target)
    }

    data class Branch(
        val condition: Conditional,
        val target: ScriptValue<*>,
        val types: Set<JumpType> = setOf()
    ) : Operation() {
        override val opCode: String =
            if (condition == Conditional.None) {
                throw UnsupportedOperationException("Cannot use Conditional.None with Branch statement. Use Jump instead.")
            } else {
                val prefix = if (JumpType.RELATIVE in types) "br" else "b"
                val suffix = if (JumpType.FUNCTION in types) "al" else ""
                prefix + condition.shortName + suffix
            }
        override val args: Array<out ScriptValue<*>> = arrayOf(*condition.args, target)
    }

    //endregion

    //region Misc Operations

    data class Alias(val alias: String, val target: Any) :
        SimpleOperation("alias", ScriptValue.of(alias), ScriptValue.of(target.toString())) {

        init {
            // TODO is there a compile-time way to do this?
            if (!(target is Register || target is Device)) {
                throw IllegalArgumentException("target must be a Register or Device")
            }
        }
    }

    data class Define(val alias: String, val value: Number) : SimpleOperation("define", ScriptValue.of(value))

    data class Comment(val message: String) : SimpleOperation("#", ScriptValue.of(message)) {
        override fun compile(context: CompileContext): CompileResults {
            return if (context.compileOptions.minify) {
                CompileResults(context)
            } else {
                super.compile(context)
            }
        }
    }

    //endregion
}
