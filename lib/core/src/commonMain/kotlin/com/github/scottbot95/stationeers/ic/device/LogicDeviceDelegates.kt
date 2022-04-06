package com.github.scottbot95.stationeers.ic.device

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline val readOnlyVar get() = readOnlyVar()
fun readOnlyVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
    LogicDeviceVarProperty(name, canRead = true, canWrite = false)

inline val writeOnlyVar get() = writeOnlyVar()
fun writeOnlyVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
    LogicDeviceVarProperty(name, canRead = false, canWrite = true)

inline val readWriteVar get() = readWriteVar()
fun readWriteVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
    LogicDeviceVarProperty(name, canRead = true, canWrite = true)

private class LogicDeviceVarProperty(
    private val name: String? = null,
    private val canRead: Boolean = true,
    private val canWrite: Boolean = false,
) : ReadOnlyProperty<LogicDevice, LogicDeviceVar> {
    override fun getValue(thisRef: LogicDevice, property: KProperty<*>): LogicDeviceVar = LogicDeviceVar(
        thisRef.port,
        name ?: property.name,
        canRead,
        canWrite
    )
}
