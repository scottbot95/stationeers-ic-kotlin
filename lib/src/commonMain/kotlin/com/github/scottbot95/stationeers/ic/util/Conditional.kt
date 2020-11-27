package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.ScriptValue

sealed class Conditional(val shortName: String, vararg val args: ScriptValue<*>) {
    object None : Conditional("") {
        override val inverse: Conditional = None
    }

    abstract val inverse: Conditional
}

class DeviceSet(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("dse", a, b) {
    override val inverse: Conditional by lazy { DeviceNotSet(a, b) }
}

class DeviceNotSet(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("dns", a, b) {
    override val inverse: Conditional by lazy { DeviceSet(a, b) }
}

class EqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("eq", a, b) {
    override val inverse: Conditional by lazy { NotEqualTo(a, b) }
}

class EqualToZero(a: ScriptValue<*>) : Conditional("eqz", a) {
    override val inverse: Conditional by lazy { NotEqualToZero(a) }
}

class NotEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ne", a, b) {
    override val inverse: Conditional by lazy { EqualTo(a, b) }
}

class NotEqualToZero(a: ScriptValue<*>) : Conditional("nez", a) {
    override val inverse: Conditional by lazy { EqualToZero(a) }
}

class ApproximatelyEqual(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ap") {
    override val inverse: Conditional by lazy { NotApproximatelyEqual(a, b) }
}

class ApproximatelyEqualZero(a: ScriptValue<*>) : Conditional("ap") {
    override val inverse: Conditional by lazy { NotApproximatelyEqualZero(a) }
}

class NotApproximatelyEqual(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("na") {
    override val inverse: Conditional by lazy { ApproximatelyEqual(b, a) }
}

class NotApproximatelyEqualZero(a: ScriptValue<*>) : Conditional("na") {
    override val inverse: Conditional by lazy { ApproximatelyEqualZero(a) }
}

class GreaterThan(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b) {
    override val inverse: Conditional by lazy { LessThanEqualTo(a, b) }
}

class GreaterThanZero(a: ScriptValue<*>) : Conditional("gt", a) {
    override val inverse: Conditional by lazy { LessThanEqualToZero(a) }
}

class GreaterThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b) {
    override val inverse: Conditional by lazy { LessThan(a, b) }
}

class GreaterThanEqualToZero(a: ScriptValue<*>) : Conditional("ge", a) {
    override val inverse: Conditional by lazy { LessThanZero(a) }
}

class LessThan(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b) {
    override val inverse: Conditional by lazy { GreaterThanEqualTo(a, b) }
}

class LessThanZero(a: ScriptValue<*>) : Conditional("gt", a) {
    override val inverse: Conditional by lazy { GreaterThanEqualToZero(a) }
}

class LessThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b) {
    override val inverse: Conditional by lazy { GreaterThan(a, b) }
}

class LessThanEqualToZero(a: ScriptValue<*>) : Conditional("ge", a) {
    override val inverse: Conditional by lazy { GreaterThanZero(a) }
}
