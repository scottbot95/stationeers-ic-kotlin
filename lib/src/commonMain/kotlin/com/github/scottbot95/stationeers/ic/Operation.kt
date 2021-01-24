package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.LineReference
import com.github.scottbot95.stationeers.ic.dsl.PartialCompiledScript
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.builder
import com.github.scottbot95.stationeers.ic.dsl.of
import com.github.scottbot95.stationeers.ic.dsl.toDouble
import com.github.scottbot95.stationeers.ic.dsl.toFixed
import com.github.scottbot95.stationeers.ic.dsl.toRelative
import com.github.scottbot95.stationeers.ic.simulation.SimulationException
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import com.github.scottbot95.stationeers.ic.simulation.SimulationStatus
import com.github.scottbot95.stationeers.ic.util.Conditional
import com.github.scottbot95.stationeers.ic.util.FlagEnum
import kotlin.math.max

enum class JumpType : FlagEnum {
    FUNCTION,
    RELATIVE,
}

typealias Hash = String

private fun makeJump(state: SimulationState, target: LineReference, functionCall: Boolean): ISimulationResults {
    val nextIP = target.toFixed().value
    return SimulationResults(
        if (functionCall) {
            state.next(Register.RA, state.instructionPointer + 1.0, nextIP)
        } else {
            state.next(nextIP)
        }
    )
}

/**
 * Generic class representing an operation within the MIPS language
 */
sealed class Operation : Compilable, Statement {

    abstract val args: Array<out ScriptValue<*>>
    abstract val opCode: String

    override fun compile(partial: PartialCompiledScript): PartialCompiledScript {
        val combined = ScriptValue.of(ScriptValue.of(opCode), *args)
        return partial.builder {
            this.addOperation(CompiledOperation(combined, statement = this@Operation))
        }
    }

    abstract class SimpleOperation internal constructor(
        override val opCode: String,
        override vararg val args: ScriptValue<*>
    ) : Operation()

    //region IO Operations

    data class Load(val output: ScriptValue<Register>, val deviceVar: LogicDeviceVar) :
        SimpleOperation("l", output, deviceVar.device, ScriptValue.of(deviceVar.name)) {

        override fun invoke(state: SimulationState): ISimulationResults {

            // TODO can we somehow bake this into the data structure holding devices?
            val device = deviceVar.device.value
            val varName = deviceVar.name
            if (device !in state.devices) {
                throw SimulationException(
                    state,
                    "Cannot read var '$varName' on disconnected device '$device'"
                )
            }

            val deviceValue = state.devices.getValue(device)[varName] ?: 0.0
            return SimulationResults(state.next(output.value, deviceValue))
        }
    }

    data class Save(val deviceVar: LogicDeviceVar, val value: ScriptValue<*>) :
        SimpleOperation("s", deviceVar.device, ScriptValue.of(deviceVar.name), value) {

        override fun invoke(state: SimulationState): ISimulationResults {
            return SimulationResults(state.next(deviceVar, value.toDouble(state)))
        }
    }

    data class BatchLoad(
        val output: ScriptValue<Register>,
        val type: ScriptValue<Hash>,
        val deviceVar: LogicDeviceVar,
        val batchMode: BatchMode
    ) : SimpleOperation("lb", output, type, ScriptValue.of(deviceVar.name), ScriptValue.of(batchMode.ordinal.toDouble())) {
        enum class BatchMode {
            AVERAGE, SUM, MINIMUM, MAXIMUM
        }

        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class BatchSave(
        val type: ScriptValue<Hash>,
        val deviceVar: LogicDeviceVar,
        val value: ScriptValue<*>
    ): SimpleOperation("sb", type, ScriptValue.of(deviceVar.name), value) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Move(val output: ScriptValue<Register>, val value: ScriptValue<*>) :
        SimpleOperation("move", output, value) {

        override fun invoke(state: SimulationState): ISimulationResults {
            val register = output.value
            if (!register.userRegister) {
                throw SimulationException(state, "Cannot write to system register $register")
            }

            return SimulationResults(state.next(register, value.toDouble(state)))
        }
    }

    //endregion

    //region Math operations

    data class Add(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("add", output, a, b) {

        override fun invoke(state: SimulationState): ISimulationResults {
            return SimulationResults(state.next(output.value, a.toDouble(state) + b.toDouble(state)))
        }
    }

    data class AbsoluteValue(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("abs", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Arccos(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("acos", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Arcsin(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("asin", output, input){
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Ceil(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("ceil", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Cos(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("cos", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Divide(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>): SimpleOperation("div", output, a,b) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Exp(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("exp", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Floor(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("floor", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Log(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("log", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Max(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("max", output, a, b) {
        override fun invoke(state: SimulationState): ISimulationResults {
            return SimulationResults(state.next(output.value, max(a.toDouble(state), b.toDouble(state))))
        }

    }

    data class Min(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("min", output, a, b) {
        override fun invoke(state: SimulationState): ISimulationResults {
            return SimulationResults(state.next(output.value, max(a.toDouble(state), b.toDouble(state))))
        }

    }

    data class Modulus(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>): SimpleOperation("mod", output, a , b) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Random(val output: ScriptValue<Register>): SimpleOperation("rand", output) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Round(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("round", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Sin(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("sin", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class SquareRoot(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("sqrt", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Tan(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("tan", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Truncate(val output: ScriptValue<Register>, val input: ScriptValue<*>): SimpleOperation("trunc", output, input) {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Subtract(val output: ScriptValue<Register>, val a: ScriptValue<*>, val b: ScriptValue<*>) :
        SimpleOperation("sub", output, a, b) {

        override fun invoke(state: SimulationState): ISimulationResults {
            return SimulationResults(state.next(output.value, a.toDouble(state) - b.toDouble(state)))
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

        override fun invoke(state: SimulationState): ISimulationResults {
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

        override fun invoke(state: SimulationState): ISimulationResults = if (condition.evaluate(state)) {
            makeJump(state, target, JumpType.FUNCTION in types)
        } else {
            SimulationResults(state.next())
        }
    }

    class Yield : SimpleOperation("yield") {
        override fun invoke(state: SimulationState): ISimulationResults {
            return SimulationResults(state.next(), SimulationStatus.Yielded)
        }
    }

    //endregion

    //region Stack operations

    data class Peek(val register: ScriptValue<Register>): SimpleOperation("peek") {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Pop(val register: ScriptValue<Register>): SimpleOperation("pop") {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    data class Push(val register: ScriptValue<*>): SimpleOperation("push") {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
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
        override fun invoke(state: SimulationState): ISimulationResults = SimulationResults(state.next())
    }

    data class Define(val alias: String, val value: Double) :
        SimpleOperation("define", ScriptValue.of(alias), ScriptValue.of(value)) {

        // TODO We should probably actually simulation alias look-ups instead of cheating...
        override fun invoke(state: SimulationState): ISimulationResults = SimulationResults(state.next())
    }

    data class Comment(val message: String) : SimpleOperation("#", ScriptValue.of(message)) {
        override fun compile(partial: PartialCompiledScript): PartialCompiledScript {
            return if (partial.compileOptions.minify) {
                partial
            } else {
                super.compile(partial)
            }
        }

        override fun invoke(state: SimulationState): ISimulationResults = SimulationResults(state.next())
    }

    class Halt(): SimpleOperation("hcf") {
        override fun invoke(state: SimulationState): ISimulationResults {
            TODO("Not yet implemented")
        }
    }

    //endregion
}
