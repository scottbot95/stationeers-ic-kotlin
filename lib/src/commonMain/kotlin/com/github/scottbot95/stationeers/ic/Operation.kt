package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.Compilable2
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
import com.github.scottbot95.stationeers.ic.dsl.CompiledLine
import com.github.scottbot95.stationeers.ic.dsl.CompiledScript
import com.github.scottbot95.stationeers.ic.dsl.LineReference
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.builder
import com.github.scottbot95.stationeers.ic.dsl.of
import com.github.scottbot95.stationeers.ic.dsl.toFixed
import com.github.scottbot95.stationeers.ic.dsl.toRelative
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import com.github.scottbot95.stationeers.ic.util.Conditional
import com.github.scottbot95.stationeers.ic.util.FlagEnum

enum class JumpType : FlagEnum {
    FUNCTION,
    RELATIVE,
}

/**
 * Generic class representing an operation within the MIPS language
 */
sealed class Operation : Compilable, Compilable2, Statement {

    abstract val args: Array<out ScriptValue<*>>
    abstract val opCode: String

    override fun compile(context: CompileContext): CompileResults {
        val parts = listOf(ScriptValue.of(opCode), *args)
        return CompileResults(context, CompiledLine(parts))
    }

    override fun compile2(compiledScript: CompiledScript): CompiledScript {
        val combined = ScriptValue.of(listOf(ScriptValue.of(opCode), *args))
        val compileContext = CompileContext(compiledScript.nextLine, compiledScript.options)
        return compiledScript.builder {
            this.addOperation(CompiledOperation(combined, compileContext, this@Operation))
        }
    }

    override fun invoke(state: SimulationState): SimulationState {
        TODO("Hack just so it compiles, this doesn't belong here")
    }

    abstract class SimpleOperation internal constructor(
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

    data class Max(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("max", output, a, b)

    data class Min(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("min", output, a, b)

    //endregion

    //region Control flow operations

    data class Jump(val target: LineReference, val type: JumpType? = null) : Operation() {
        override val opCode: String = when (type) {
            JumpType.FUNCTION -> "jal"
            JumpType.RELATIVE -> "jr"
            null -> "j"
        }

        override val args: Array<out ScriptValue<*>> =
            arrayOf(if (type === JumpType.RELATIVE) target.toRelative() else target.toFixed())
    }

    data class Branch(
        val condition: Conditional,
        val target: LineReference,
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
        override val args: Array<out ScriptValue<*>> =
            arrayOf(*condition.args, if (types.contains(JumpType.RELATIVE)) target.toRelative() else target.toFixed())
    }

    class Yield : SimpleOperation("yield")

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

    data class Define(val alias: String, val value: Number) :
        SimpleOperation("define", ScriptValue.of(alias), ScriptValue.of(value))

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
