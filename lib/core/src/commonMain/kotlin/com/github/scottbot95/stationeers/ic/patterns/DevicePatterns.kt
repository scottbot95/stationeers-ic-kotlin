package com.github.scottbot95.stationeers.ic.patterns

import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.ScriptValue
import com.github.scottbot95.stationeers.ic.appendInstruction
import com.github.scottbot95.stationeers.ic.instructions.Flow.Branch
import com.github.scottbot95.stationeers.ic.instructions.Flow.Conditional.DeviceNotConnected
import com.github.scottbot95.stationeers.ic.instructions.Flow.Jump
import com.github.scottbot95.stationeers.ic.instructions.Misc
import com.github.scottbot95.stationeers.ic.newLineReference
import com.github.scottbot95.stationeers.ic.util.asIntRegister

fun ICScriptBuilder.waitTillConnected(vararg devices: ScriptValue.DeviceReference, functionCall: Boolean = false) {
    if (devices.isEmpty()) {
        // TODO log a warning here
        return
    }

    val start = newLineReference("WaitTillConnected")
    appendEntry(start.mark)
    appendInstruction(Misc.Yield)
    devices.forEach {
        appendInstruction(Branch(DeviceNotConnected(it), start))
    }

    if (functionCall) appendInstruction(Jump(Register.RA.asIntRegister()))
}
