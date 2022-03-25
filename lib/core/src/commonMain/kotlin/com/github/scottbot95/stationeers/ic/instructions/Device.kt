package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.DeviceLiteral
import com.github.scottbot95.stationeers.ic.NumberLiteral.IntValue
import com.github.scottbot95.stationeers.ic.ScriptValue
import com.github.scottbot95.stationeers.ic.ScriptValue.NumberValue
import com.github.scottbot95.stationeers.ic.StringValue

object Device {
    data class Load(val dest: ScriptValue.RegisterValue<*>, val device: DeviceLiteral, val setting: StringValue) :
        Instruction("l", dest, device, setting)

    data class BatchSave(val typeHash: NumberValue<Int>, val setting: StringValue, val value: NumberValue<*>) :
        Instruction("sb", typeHash, setting, value)
}