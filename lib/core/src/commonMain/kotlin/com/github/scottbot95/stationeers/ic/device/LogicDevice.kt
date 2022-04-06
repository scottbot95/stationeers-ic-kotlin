package com.github.scottbot95.stationeers.ic.device

import com.github.scottbot95.stationeers.ic.ScriptValue.DeviceReference

data class LogicDeviceVar(
    val device: DeviceReference,
    val name: String,
    val canRead: Boolean,
    val canWrite: Boolean,
)

interface LogicDevice {
    val port: DeviceReference
}

abstract class LogicDeviceBase(
    override val port: DeviceReference
) : LogicDevice

//region Common Device Types
val LogicDevice.PrefabHash by readOnlyVar

interface LockableDevice : LogicDevice

val LockableDevice.Lock by readWriteVar

interface PoweredLogicDevice : LogicDevice

val PoweredLogicDevice.On by readWriteVar
val PoweredLogicDevice.Powered by readOnlyVar
val PoweredLogicDevice.RequiredPower by readOnlyVar

interface ChargedDevice : LogicDevice

val ChargedDevice.Charge by readOnlyVar

/**
 * Mutually exclusive with [ReadableDevice]
 */
interface SettableDevice : LogicDevice

val SettableDevice.Setting by readWriteVar

/**
 * Mutually exclusive with [SettableDevice]
 */
interface ReadableDevice : LogicDevice

val ReadableDevice.Setting by readOnlyVar

interface AtmosphericDevice : LogicDevice

val AtmosphericDevice.Temperature by readOnlyVar
val AtmosphericDevice.Pressure by readOnlyVar
val AtmosphericDevice.RatioCarbonDioxide by readOnlyVar
val AtmosphericDevice.RatioNitrogen by readOnlyVar
val AtmosphericDevice.RatioNitrousOxide by readOnlyVar
val AtmosphericDevice.RatioOxygen by readOnlyVar
val AtmosphericDevice.RatioPollutant by readOnlyVar
val AtmosphericDevice.RatioVolatiles by readOnlyVar
val AtmosphericDevice.RatioWater by readOnlyVar

interface CountingDevice : LogicDevice

val CountingDevice.ClearMemory by writeOnlyVar
val CountingDevice.ImportCount by readOnlyVar
val CountingDevice.ExportCount by readOnlyVar

/**
 * Mutually exclusive with [ConfigurableCraftingDevice]
 */
interface CraftingDevice : LogicDevice

val CraftingDevice.RecipeHash by readOnlyVar

/**
 * Mutually exclusive with [CraftingDevice]
 */
interface ConfigurableCraftingDevice : LogicDevice

val ConfigurableCraftingDevice.RecipeHash by readWriteVar

interface ReagentDevice : LogicDevice

val ReagentDevice.Reagents by readOnlyVar

interface ErrorDevice : LogicDevice

val ErrorDevice.Error by readOnlyVar

interface OpenableDevice : LogicDevice

val OpenableDevice.Open by readWriteVar

interface ActivateableDevice : LogicDevice

val ActivateableDevice.Activate by readWriteVar

//endregion

//region Concrete device classes
class LogicMemory(device: DeviceReference) : LogicDeviceBase(device), SettableDevice

class DaylightSensor(device: DeviceReference) : LogicDeviceBase(device) {
    val Mode by readWriteVar
    val SolarAngle by readOnlyVar
}

class Light(device: DeviceReference) :
    LogicDeviceBase(device),
    PoweredLogicDevice,
    LockableDevice

class Switch(device: DeviceReference) :
    LogicDeviceBase(device),
    LockableDevice,
    ReadableDevice,
    OpenableDevice

class SolarPanel(device: DeviceReference) : LogicDeviceBase(device), ChargedDevice {
    /** Horizontal rotation of Solar Panel in degrees. Data port is 270 degrees */
    val Horizontal by readWriteVar

    /** Percentage vertical rotation of Solar Panel.*/
    val Vertical by readWriteVar

    /** Theoretical max power output on this planet in Watts */
    val Maximum by readOnlyVar

    /** Ratio of [Maximum] to current power output */
    val Ratio by readOnlyVar
}

class AdvFurnace(device: DeviceReference) :
    LogicDeviceBase(device),
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
//endregion
