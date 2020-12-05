package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
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
import com.github.scottbot95.stationeers.ic.dsl.toDouble
import com.github.scottbot95.stationeers.ic.dsl.toFixed
import com.github.scottbot95.stationeers.ic.dsl.toRelative
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import com.github.scottbot95.stationeers.ic.util.Conditional
import com.github.scottbot95.stationeers.ic.util.FlagEnum
import kotlin.math.max

enum class JumpType : FlagEnum {
    FUNCTION,
    RELATIVE,
}

private fun makeJump(state: SimulationState, target: LineReference, functionCall: Boolean): SimulationState {
    val nextIP = target.toFixed().value
    return if (functionCall) {
        state.next(Register.RA, state.instructionPointer + 1.0, nextIP)
    } else {
        state.next(nextIP)
    }
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

    abstract class SimpleOperation internal constructor(
        override val opCode: String,
        override vararg val args: ScriptValue<*>
    ) : Operation()

    //region IO Operations

    data class Load(val output: ScriptValue<Register>, val deviceVar: LogicDeviceVar) :
        SimpleOperation("l", output, deviceVar.device, ScriptValue.of(deviceVar.name)) {

        override fun invoke(state: SimulationState): SimulationState {
            val deviceValue = state.devices.getValue(deviceVar.device.value)[deviceVar.name] ?: 0.0
            return state.next(output.value, deviceValue)
        }
    }

    data class Save(val deviceVar: LogicDeviceVar, val value: ScriptValue<*>) :
        SimpleOperation("s", deviceVar.device, ScriptValue.of(deviceVar.name), value) {

        override fun invoke(state: SimulationState): SimulationState {
            return state.next(deviceVar, value.toDouble(state))
        }
    }

    data class Move(val output: ScriptValue<Register>, val value: ScriptValue<*>) :
        SimpleOperation("move", output, value) {

        override fun invoke(state: SimulationState): SimulationState {
            return state.next(output.value, value.toDouble(state))
        }
    }

    //endregion

    //region Math operations

    data class Add(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("add", output, a, b) {

        override fun invoke(state: SimulationState): SimulationState {
            return state.next(output.value, a.toDouble(state) + b.toDouble(state))
        }
    }

    data class Subtract(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("sub", output, a, b) {

        override fun invoke(state: SimulationState): SimulationState {
            return state.next(output.value, a.toDouble(state) - b.toDouble(state))
        }
    }

    data class Max(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("max", output, a, b) {

        override fun invoke(state: SimulationState): SimulationState {
            return state.next(output.value, max(a.toDouble(state), b.toDouble(state)))
        }
    }

    data class Min(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("min", output, a, b) {

        override fun invoke(state: SimulationState): SimulationState {
            return state.next(output.value, max(a.toDouble(state), b.toDouble(state)))
        }
    }

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

        override fun invoke(state: SimulationState): SimulationState {
            return when (type) {
                JumpType.FUNCTION -> makeJump(state, target, true)
                JumpType.RELATIVE, null -> makeJump(state, target, false)
            }
        }
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

        override fun invoke(state: SimulationState): SimulationState = if (condition.evaluate(state)) {
            makeJump(state, target, JumpType.FUNCTION in types)
        } else {
            state.next()
        }
    }

    class Yield : SimpleOperation("yield") {
        override fun invoke(state: SimulationState): SimulationState {
            TODO("Figure out the yield hook stuff")
        }
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

        // TODO We should probably actually simulation alias look-ups instead of cheating...
        override fun invoke(state: SimulationState): SimulationState = state.next()
    }

    data class Define(val alias: String, val value: Double) :
        SimpleOperation("define", ScriptValue.of(alias), ScriptValue.of(value)) {

        // TODO We should probably actually simulation alias look-ups instead of cheating...
        override fun invoke(state: SimulationState): SimulationState = state.next()
    }

    data class Comment(val message: String) : SimpleOperation("#", ScriptValue.of(message)) {
        override fun compile(context: CompileContext): CompileResults {
            return if (context.compileOptions.minify) {
                CompileResults(context)
            } else {
                super.compile(context)
            }
        }

        override fun invoke(state: SimulationState): SimulationState = state.next()
    }

    //endregion
}
