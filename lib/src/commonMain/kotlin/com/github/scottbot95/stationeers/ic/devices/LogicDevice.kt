@file:Suppress("PropertyName")

package com.github.scottbot95.stationeers.ic.devices

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.dsl.AliasedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.SimpleAliasedScriptValue

data class LogicDeviceVar(
    val device: LogicDevice,
    val name: String,
    val canRead: Boolean,
    val canWrite: Boolean
)

interface LogicDevice : AliasedScriptValue<Device>

abstract class LogicDeviceBase(
    alias: String?,
    device: ScriptValue<Device>,
) : SimpleAliasedScriptValue<Device>(alias, device), LogicDevice

class LogicMemory(
    alias: String?,
    device: ScriptValue<Device>,
) : LogicDeviceBase(alias, device), SettableDevice

class DaylightSensor(
    alias: String?,
    device: ScriptValue<Device>
): LogicDeviceBase(alias, device) {
    val Mode by readWriteVar
    val SolarAngle by readOnlyVar
}

class Light(alias: String?, device: ScriptValue<Device>) :
    LogicDeviceBase(alias, device),
    PoweredLogicDevice,
    LockableDevice

class Switch(alias: String?, device: ScriptValue<Device>) :
    LogicDeviceBase(alias, device),
    LockableDevice,
    ReadableDevice,
    OpenableDevice

class AdvFurnace(alias: String?, device: ScriptValue<Device>) :
    LogicDeviceBase(alias, device),
    PoweredLogicDevice,
    LockableDevice,
    SettableDevice,
    AtmosphericDevice,
    CountingDevice,
    CraftingDevice,
    ReagentDevice,
    ErrorDevice,
    OpenableDevice,
    ActivateableDevice {
    val SettingOutput by readWriteVar
    val SettingInput by readWriteVar
}
