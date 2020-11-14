package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.CompileOptions
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue

enum class Device {
    D0, D1, D2, D3, D4, D5;

    override fun toString(): String = name.toLowerCase()
}

class DeviceValue(override val value: Device) : ScriptValue<Device> {
    override fun toString(options: CompileOptions): String = value.toString()
}
