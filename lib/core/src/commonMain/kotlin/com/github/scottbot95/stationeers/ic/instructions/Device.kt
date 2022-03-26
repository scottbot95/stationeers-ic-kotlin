package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.DeviceLiteral
import com.github.scottbot95.stationeers.ic.ScriptValue
import com.github.scottbot95.stationeers.ic.ScriptValue.NumberValue
import com.github.scottbot95.stationeers.ic.StringLiteral

object Device {
    data class Load(val dest: ScriptValue.RegisterValue<*>, val device: DeviceLiteral, val setting: StringLiteral) :
        Instruction("l", dest, device, setting)

    data class BatchSave(val typeHash: NumberValue<Int>, val setting: StringLiteral, val value: NumberValue<*>) :
        Instruction("sb", typeHash, setting, value)
}