package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.devices.LogicDevice
import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueConstructor
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueDelegateProvider
import com.github.scottbot95.stationeers.ic.util.DefaultAliasedScriptValueDelegateProvider
import kotlin.properties.ReadOnlyProperty

// #region Delegate extensions

inline fun ScriptBlock.registers(init: AliasedScriptValueContainer<Register>.() -> Unit): AliasedScriptValueContainer<Register> {
    registers.init()
    return registers
}

inline val ScriptBlock.register: DefaultAliasedScriptValueDelegateProvider<Register> get() = register()

fun ScriptBlock.register(name: String): DefaultAliasedScriptValueDelegateProvider<Register> = register(null, name)

fun ScriptBlock.register(
    register: Register? = null,
    name: String? = null
): DefaultAliasedScriptValueDelegateProvider<Register> {
    return DefaultAliasedScriptValueDelegateProvider(registers, register, name)
}

inline fun ScriptBlock.devices(init: AliasedScriptValueContainer<Device>.() -> Unit): AliasedScriptValueContainer<Device> {
    devices.init()
    return devices
}

fun <T : LogicDevice> ScriptBlock.device(
    deviceConstructor: AliasedScriptValueConstructor<Device, T>,
    device: Device? = null,
    name: String? = null,
): AliasedScriptValueDelegateProvider<Device, T> {
    return AliasedScriptValueDelegateProvider(devices, device, name, deviceConstructor)
}

// TODO should probably use an AliasedScriptValueContainer for this...
fun ScriptBlock.define(name: String, value: Number): ScriptValue<Number> {
    +Operation.Define(name, value)
    return AliasedScriptValue(name, ScriptValue.of(value))
}

fun ScriptBlock.define(value: Number, name: String? = null) =
    ReadOnlyProperty<Any?, ScriptValue<Number>> { _, prop -> define(name ?: prop.name, value) }

// #endregion

// #region Basic Operation extensions

inline fun ScriptBlock.block(init: ScriptBlock.() -> Unit): ScriptBlock {
    val block = SimpleScriptBlock(this)
    block.init()
    +block
    return block
}

fun ScriptBlock.comment(message: String) {
    +Operation.Comment(message)
}

fun ScriptBlock.add(output: ScriptValue<Register>, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Add(output, a, b)
}

/**
 * Allocates a temporary register and reads [deviceVar] from [device]
 */
fun ScriptBlock.readDevice(device: ScriptValue<Device>, deviceVar: String): ScriptValue<Register> =
    registers.newAliasedValue(null, null).also {
        +Operation.Load(it, device, deviceVar)
    }

fun ScriptBlock.readDevice(deviceVar: LogicDeviceVar) = readDevice(deviceVar.device, deviceVar.name)

fun ScriptBlock.readDevice(output: ScriptValue<Register>, device: ScriptValue<Device>, deviceVar: String) {
    +Operation.Load(output, device, deviceVar)
}

fun ScriptBlock.readDevice(output: ScriptValue<Register>, deviceVar: LogicDeviceVar) {
    if (!deviceVar.canRead) {
        throw IllegalArgumentException("Cannot read from ${deviceVar.name} on ${deviceVar.device.alias ?: deviceVar.device.value}")
    }
    return readDevice(output, deviceVar.device, deviceVar.name)
}

fun ScriptBlock.writeDevice(device: ScriptValue<Device>, deviceVar: String, value: ScriptValue<*>) {
    +Operation.Save(device, deviceVar, value)
}

fun ScriptBlock.writeDevice(deviceVar: LogicDeviceVar, value: ScriptValue<*>) {
    if (!deviceVar.canWrite) {
        throw IllegalArgumentException("Cannot write to ${deviceVar.name} on ${deviceVar.device.alias ?: deviceVar.device.value}")
    }
    return writeDevice(deviceVar.device, deviceVar.name, value)
}

// #endregion

// #region Composite Operation extensions

inline fun ScriptBlock.forever(
    label: String? = null,
    shouldYield: Boolean = true,
    init: ScriptBlock.() -> Unit
): ScriptBlock =
    LoopingScriptBlock(label, shouldYield = shouldYield, scope = this).also {
        it.init()
        +it
    }

// TODO Not sure I like this...
fun ScriptBlock.inc(register: ScriptValue<Register>) {
    +Operation.Add(register, register, ScriptValue.of(1))
}

// #endregion
