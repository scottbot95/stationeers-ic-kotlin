package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Expression
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.of
import com.github.scottbot95.stationeers.ic.dsl.toNumber
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import kotlin.math.abs
import kotlin.math.max

private fun Any?.isZero(): Boolean = this is Number && this.toInt() == 0

private fun approximatelyEqual(a: Double, b: Double, c: Double): Boolean =
    abs(a - b) <= max(c * max(abs(a), abs(b)), Float.MIN_VALUE.toDouble() * 8)

private fun approximatelyZero(a: Double): Boolean = abs(a) <= Float.MIN_VALUE.toDouble() * 8

sealed class Conditional(val shortName: String, vararg val args: ScriptValue<*>) : Expression<Boolean> {
    abstract val inverse: Conditional

    /**
     * The [Conditional] that is always true
     */
    object None : Conditional("") {

        override val inverse: Conditional = None
        override fun evaluate(state: SimulationState) = true
    }
}

abstract class NumberConditional(
    shortName: String,
    private vararg val values: ScriptValue<*>
) : Conditional(shortName, *values) {

    override fun evaluate(state: SimulationState): Boolean = evaluate(values.map { it.toNumber(state).toDouble() })

    protected abstract fun evaluate(values: List<Double>): Boolean
}

abstract class UnaryConditional(
    shortName: String,
    a: ScriptValue<*>
) : NumberConditional(shortName, a) {
    override fun evaluate(values: List<Double>): Boolean = evaluate(values[0])

    protected abstract fun evaluate(a: Double): Boolean
}

abstract class BinomialConditional(
    shortName: String,
    a: ScriptValue<*>,
    b: ScriptValue<*>
) : NumberConditional(
    shortName + if (a.value.isZero() || b.value.isZero()) "z" else "",
    *listOf(a, b).filter { !it.value.isZero() }.let {
        if (it.isEmpty()) { // Ensure we don't prune both args
            listOf(ScriptValue.of(0))
        } else {
            it
        }
    }.toTypedArray(),
) {
    override fun evaluate(values: List<Double>): Boolean = evaluate(values[0], values[1])

    protected abstract fun evaluate(a: Double, b: Double): Boolean
}

abstract class TrinomialConditional(
    shortName: String,
    a: ScriptValue<*>,
    b: ScriptValue<*>,
    c: ScriptValue<*>
) : NumberConditional(shortName, a, b, c) {
    override fun evaluate(values: List<Double>): Boolean = evaluate(values[0], values[1], values[2])

    protected abstract fun evaluate(a: Double, b: Double, c: Double): Boolean
}

class DeviceSet(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("dse", a, b) {
    override val inverse: Conditional by lazy { DeviceNotSet(a, b) }

    override fun evaluate(state: SimulationState): Boolean {
        TODO("Not yet implemented")
    }
}

class DeviceNotSet(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("dns", a, b) {
    override val inverse: Conditional by lazy { DeviceSet(a, b) }

    override fun evaluate(state: SimulationState): Boolean {
        TODO("Not yet implemented")
    }
}

// TODO Not sure I like this being open...
open class EqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : BinomialConditional("eq", a, b) {
    // Maybe add an epsilon here?
    override fun evaluate(a: Double, b: Double): Boolean = a == b

    override val inverse: Conditional by lazy { NotEqualTo(a, b) }
}

class EqualToZero(a: ScriptValue<*>) : EqualTo(a, ScriptValue.of(0)) {
    override val inverse: Conditional by lazy { NotEqualToZero(a) }
}

open class NotEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : BinomialConditional("ne", a, b) {
    override fun evaluate(a: Double, b: Double): Boolean = a != b

    override val inverse: Conditional by lazy { EqualTo(a, b) }
}

class NotEqualToZero(a: ScriptValue<*>) : NotEqualTo(a, ScriptValue.of(0)) {
    override val inverse: Conditional by lazy { EqualToZero(a) }
}

class ApproximatelyEqual(a: ScriptValue<*>, b: ScriptValue<*>, c: ScriptValue<*>) :
    TrinomialConditional("ap", a, b, c) {
    override fun evaluate(a: Double, b: Double, c: Double): Boolean = approximatelyEqual(a, b, c)

    override val inverse: Conditional by lazy { NotApproximatelyEqual(a, b, c) }
}

class ApproximatelyEqualZero(private val a: ScriptValue<*>) : UnaryConditional("apz", a) {
    override fun evaluate(a: Double): Boolean = approximatelyZero(a)

    override val inverse: Conditional by lazy { NotApproximatelyEqualZero(a) }
}

class NotApproximatelyEqual(a: ScriptValue<*>, b: ScriptValue<*>, c: ScriptValue<*>) :
    TrinomialConditional("na", a, b, c) {
    override fun evaluate(a: Double, b: Double, c: Double): Boolean = !approximatelyEqual(a, b, c)

    override val inverse: Conditional by lazy { ApproximatelyEqual(a, b, c) }
}

class NotApproximatelyEqualZero(a: ScriptValue<*>) : UnaryConditional("naz", a) {
    override fun evaluate(a: Double): Boolean = !approximatelyZero(a)

    override val inverse: Conditional by lazy { ApproximatelyEqualZero(a) }
}

open class GreaterThan(a: ScriptValue<*>, b: ScriptValue<*>) : BinomialConditional("gt", a, b) {
    override fun evaluate(a: Double, b: Double): Boolean = a > b

    override val inverse: Conditional by lazy { LessThanEqualTo(a, b) }
}

class GreaterThanZero(a: ScriptValue<*>) : GreaterThan(a, ScriptValue.of(0)) {
    override val inverse: Conditional by lazy { LessThanEqualToZero(a) }
}

open class GreaterThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : BinomialConditional("ge", a, b) {
    override fun evaluate(a: Double, b: Double): Boolean = a >= b

    override val inverse: Conditional by lazy { LessThan(a, b) }
}

class GreaterThanEqualToZero(a: ScriptValue<*>) : GreaterThanEqualTo(a, ScriptValue.of(0)) {
    override val inverse: Conditional by lazy { LessThanZero(a) }
}

open class LessThan(a: ScriptValue<*>, b: ScriptValue<*>) : BinomialConditional("lt", a, b) {
    override fun evaluate(a: Double, b: Double): Boolean = a < b

    override val inverse: Conditional by lazy { GreaterThanEqualTo(a, b) }
}

class LessThanZero(a: ScriptValue<*>) : LessThan(a, ScriptValue.of(0)) {
    override val inverse: Conditional by lazy { GreaterThanEqualToZero(a) }
}

open class LessThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : BinomialConditional("le", a, b) {
    override fun evaluate(a: Double, b: Double): Boolean = a >= b

    override val inverse: Conditional by lazy { GreaterThan(a, b) }
}

class LessThanEqualToZero(a: ScriptValue<*>) : LessThanEqualTo(a, ScriptValue.of(0)) {
    override val inverse: Conditional by lazy { GreaterThanZero(a) }
}
