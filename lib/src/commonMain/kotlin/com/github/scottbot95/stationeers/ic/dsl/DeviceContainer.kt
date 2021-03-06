package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer

class DeviceContainer : AliasedScriptValueContainer<Device>("d") {
    override fun nextFreeValue(): Device? = Device.values().firstOrNull { getUsed(it) == 0 }

    override fun newInstance(value: Device): ScriptValue<Device> = ScriptValue.of(value)
}
