package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.ScriptValue.JumpTarget
import com.github.scottbot95.stationeers.ic.ScriptValue.NumberValue

object Branch {
    data class Jump(val target: JumpTarget<*>) : Instruction("j", target)

    // TODO Make some generic branch class that can accept a conditional
    data class BranchLessThan(val a: NumberValue<*>, val b: NumberValue<*>, val target: JumpTarget<*>) :
        Instruction("blt", a, b, target)
}
