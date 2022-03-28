package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.CodeBlockBuilder
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.ScriptValue
import com.github.scottbot95.stationeers.ic.instructions.Flow.Branch
import com.github.scottbot95.stationeers.ic.instructions.Flow.Conditional.DeviceNotConnected
import com.github.scottbot95.stationeers.ic.instructions.Flow.Jump
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.util.asIntRegister

fun <T : CodeBlockBuilder<T>> T.waitTillConnected(
    vararg devices: ScriptValue.DeviceReference,
    functionCall: Boolean = false
): T {
    if (devices.isEmpty()) {
        // TODO log a warning here
        return this
    }

    val start = newLineReference("WaitTillConnected")
    appendEntry(start.mark)
    appendEntry(Misc.Yield)
    devices.forEach {
        appendEntry(Branch(DeviceNotConnected(it), start))
    }

    if (functionCall) appendEntry(Jump(Register.RA.asIntRegister()))

    return this
}
