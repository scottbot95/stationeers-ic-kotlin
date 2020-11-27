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

class Approximately(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ap") {
    override val inverse: Conditional by lazy { NotApproximately(a, b) }
}

class ApproximatelyZero(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ap") {
    override val inverse: Conditional by lazy { NotApproximatelyZero(a, b) }
}

class NotApproximately(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("na") {
    override val inverse: Conditional by lazy { Approximately(b, a) }
}

class NotApproximatelyZero(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("na") {
    override val inverse: Conditional by lazy { ApproximatelyZero(b, a) }
}

class GreaterThan(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b) {
    override val inverse: Conditional by lazy { LessThanEqualTo(a, b) }
}

class GreaterThanZero(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b) {
    override val inverse: Conditional by lazy { LessThanEqualToZero(a, b) }
}

class GreaterThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b) {
    override val inverse: Conditional by lazy { LessThan(a, b) }
}

class GreaterThanEqualToZero(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b) {
    override val inverse: Conditional by lazy { LessThanZero(a, b) }
}

class LessThan(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b) {
    override val inverse: Conditional by lazy { GreaterThanEqualTo(a, b) }
}

class LessThanZero(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b) {
    override val inverse: Conditional by lazy { GreaterThanEqualToZero(a, b) }
}

class LessThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b) {
    override val inverse: Conditional by lazy { GreaterThan(a, b) }
}

class LessThanEqualToZero(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b) {
    override val inverse: Conditional by lazy { GreaterThanZero(a, b) }
}
