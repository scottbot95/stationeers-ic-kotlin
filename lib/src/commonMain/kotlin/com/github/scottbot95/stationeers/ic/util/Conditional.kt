package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.ScriptValue

sealed class Conditional(val shortName: String, vararg val args: ScriptValue<*>) {
    object None : Conditional("")
}

class EqualToZero(a: ScriptValue<*>) : Conditional("eqz", a)

class GreaterThan(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("gt", a, b)

class GreaterThanEqualTo(a: ScriptValue<*>, b: ScriptValue<*>) : Conditional("ge", a, b)
