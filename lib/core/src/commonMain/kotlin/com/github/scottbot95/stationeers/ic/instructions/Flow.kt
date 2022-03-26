package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.ScriptValue
import com.github.scottbot95.stationeers.ic.ScriptValue.JumpTarget
import com.github.scottbot95.stationeers.ic.ScriptValue.NumberValue
import com.github.scottbot95.stationeers.ic.util.toScriptValue

object Flow {
    sealed interface JumpMode {
        object Normal : JumpMode
        object Relative : JumpMode
        object FunctionCall : JumpMode
    }

    sealed class Conditional(val opCode: String, vararg val operands: ScriptValue<*>) {
        data class DeviceConnected(val device: ScriptValue.DeviceReference) : Conditional("dse", device) {
            override val inverse: DeviceNotConnected = DeviceNotConnected(device)
        }

        data class DeviceNotConnected(val device: ScriptValue.DeviceReference) : Conditional("dns", device) {
            override val inverse: DeviceConnected = DeviceConnected(device)
        }

        // abs(a - b) <= max(c * max(abs(a), abs(b)), float.epsilon * 8)
        data class ApproximatelyEqual(
            val a: NumberValue<*>,
            val b: NumberValue<*>,
            val c: NumberValue<*> = 0.toScriptValue()
        ) : Conditional("ap", a, b, c) {
            override val inverse: NotApproximatelyEqual = NotApproximatelyEqual(a, b, c)
        }

        // abs(a) <= max(c*a, float.epsilon * 8)
        data class ApproximatelyZero(
            val a: NumberValue<*>,
            val b: NumberValue<*> = 0.toScriptValue()
        ) : Conditional("apz", a, b) {
            override val inverse: NotApproximatelyZero = NotApproximatelyZero(a, b)
        }

        data class NotApproximatelyEqual(
            val a: NumberValue<*>,
            val b: NumberValue<*>,
            val c: NumberValue<*> = 0.toScriptValue()
        ) : Conditional("na", a, b, c) {
            override val inverse: ApproximatelyEqual = ApproximatelyEqual(a, b, c)
        }

        data class NotApproximatelyZero(
            val a: NumberValue<*>,
            val b: NumberValue<*> = 0.toScriptValue()
        ) : Conditional("naz", a, b) {
            override val inverse: ApproximatelyZero = ApproximatelyZero(a, b)
        }

        data class EqualTo(val a: NumberValue<*>, val b: NumberValue<*>) : Conditional("eq", a, b) {
            override val inverse: NotEqualTo = NotEqualTo(a, b)
        }

        data class EqualToZero(val a: NumberValue<*>) : Conditional("eqz", a) {
            override val inverse: NotEqualToZero = NotEqualToZero(a)
        }

        data class NotEqualTo(val a: NumberValue<*>, val b: NumberValue<*>) : Conditional("ne", a, b) {
            override val inverse: EqualTo = EqualTo(a, b)
        }

        data class NotEqualToZero(val a: NumberValue<*>) : Conditional("nez", a) {
            override val inverse: EqualToZero = EqualToZero(a)
        }

        data class GreaterThanEqualTo(val a: NumberValue<*>, val b: NumberValue<*>) : Conditional("ge", a, b) {
            override val inverse: LessThan = LessThan(a, b)
        }

        data class GreaterThanEqualToZero(val a: NumberValue<*>) : Conditional("gez", a) {
            override val inverse: LessThanZero = LessThanZero(a)
        }

        data class GreaterThan(val a: NumberValue<*>, val b: NumberValue<*>) : Conditional("gt", a, b) {
            override val inverse: LessThanEqualTo = LessThanEqualTo(a, b)
        }

        data class GreaterThanZero(val a: NumberValue<*>) : Conditional("gtz", a) {
            override val inverse: LessThanEqualToZero = LessThanEqualToZero(a)
        }

        data class LessThanEqualTo(val a: NumberValue<*>, val b: NumberValue<*>) : Conditional("le", a, b) {
            override val inverse: GreaterThan = GreaterThan(a, b)
        }

        data class LessThanEqualToZero(val a: NumberValue<*>) : Conditional("lez", a) {
            override val inverse: GreaterThanZero = GreaterThanZero(a)
        }

        data class LessThan(val a: NumberValue<*>, val b: NumberValue<*>) : Conditional("lt", a, b) {
            override val inverse: GreaterThanEqualTo = GreaterThanEqualTo(a, b)
        }

        data class LessThanZero(val a: NumberValue<*>) : Conditional("ltz", a) {
            override val inverse: GreaterThanEqualToZero = GreaterThanEqualToZero(a)
        }

        abstract val inverse: Conditional
    }

    data class Jump(val target: JumpTarget<*>, val jumpMode: JumpMode = JumpMode.Normal) :
        Instruction("j" + jumpMode.prefix + jumpMode.suffix, target)

    data class Branch(
        val conditional: Conditional,
        val target: JumpTarget<*>,
        val jumpMode: JumpMode = JumpMode.Normal
    ) : Instruction(
        "b" + jumpMode.prefix + conditional.opCode + jumpMode.suffix,
        *conditional.operands,
        target
    )
}

private inline val Flow.JumpMode.prefix: String
    get() = if (this is Flow.JumpMode.Relative) "r" else ""

private inline val Flow.JumpMode.suffix: String
    get() = if (this is Flow.JumpMode.FunctionCall) "al" else ""
