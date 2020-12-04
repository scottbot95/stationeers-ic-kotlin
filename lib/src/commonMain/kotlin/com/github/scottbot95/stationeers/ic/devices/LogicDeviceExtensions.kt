package com.github.scottbot95.stationeers.ic.devices

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

//region Delegate functions

// TODO it would be nice if these were extensions of LogicDevice.
//  That doesn't seem to work with the delegated extension properties unfortunately
inline val readOnlyVar get() = readOnlyVar()
fun readOnlyVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
    LogicDeviceProperty(name)

inline val readWriteVar get() = readWriteVar()
fun readWriteVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
    LogicDeviceProperty(name, canWrite = true)

inline val writeOnlyVar get() = writeOnlyVar()
fun writeOnlyVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
    LogicDeviceProperty(name, canRead = false, canWrite = true)

private class LogicDeviceProperty<T : LogicDevice>(
    private val name: String? = null,
    private val canRead: Boolean = true,
    private val canWrite: Boolean = false
) :
    ReadOnlyProperty<T, LogicDeviceVar> {
    override fun getValue(thisRef: T, property: KProperty<*>) =
        LogicDeviceVar(thisRef, name ?: property.name, canRead, canWrite)
}

//endregion

//region Extensions for common types of devices

// TODO should this be on a StructureDevice interface?
//  Would need to better understand Stationeers logic devices. Maybe we can get a data-dump with device vars?
val LogicDevice.PrefabHash by readOnlyVar

interface LockableDevice : LogicDevice
val LockableDevice.Lock by readWriteVar

interface PoweredLogicDevice : LogicDevice
val PoweredLogicDevice.On by readWriteVar
val PoweredLogicDevice.Powered by readOnlyVar
val PoweredLogicDevice.RequiredPower by readOnlyVar

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
