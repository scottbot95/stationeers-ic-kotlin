@file:Suppress("PropertyName")

package com.github.scottbot95.stationeers.ic.devices

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.dsl.AliasedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LogicDeviceVar(
    val device: LogicDevice,
    val name: String,
    val canRead: Boolean,
    val canWrite: Boolean
)

abstract class LogicDevice(
    alias: String?,
    private val device: ScriptValue<Device>,
    release: () -> Unit
) : AliasedScriptValue<Device>(alias, device, release) {
    protected val readWriteVar = readWriteVar()

    protected fun readWriteVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
        LogicDeviceProperty(this, name, canWrite = true)

    protected val readOnlyVar = readOnlyVar()
    protected fun readOnlyVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
        LogicDeviceProperty(this, name)

    protected val writeOnlyVar = writeOnlyVar()
    protected fun writeOnlyVar(name: String? = null): ReadOnlyProperty<LogicDevice, LogicDeviceVar> =
        LogicDeviceProperty(this, name, canRead = false, canWrite = true)

    private class LogicDeviceProperty(
        private val device: LogicDevice,
        private val name: String? = null,
        private val canRead: Boolean = true,
        private val canWrite: Boolean = false
    ) :
        ReadOnlyProperty<LogicDevice, LogicDeviceVar> {
        override fun getValue(thisRef: LogicDevice, property: KProperty<*>) =
            LogicDeviceVar(device, name ?: property.name, canRead, canWrite)
    }
}

class Light(alias: String?, device: ScriptValue<Device>, release: () -> Unit) : LogicDevice(alias, device, release) {
    val On by readWriteVar
    val RequiredPower by readOnlyVar
    val Powered by readOnlyVar
    val Lock by readWriteVar
}

class Switch(alias: String?, device: ScriptValue<Device>, release: () -> Unit) : LogicDevice(alias, device, release) {
    val Open by readWriteVar
    val Lock by readWriteVar
    val Setting by readOnlyVar
}
