package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.ScriptValue.NumberValue
import com.github.scottbot95.stationeers.ic.ScriptValue.RegisterValue

object Math {
    data class Subtract(val dest: RegisterValue<*>, val a: NumberValue<*>, val b: NumberValue<*>) :
        Instruction("sub", dest, a, b)

    data class Divide(val dest: RegisterValue<*>, val a: NumberValue<*>, val b: NumberValue<*>) :
        Instruction("div", dest, a, b)
}
