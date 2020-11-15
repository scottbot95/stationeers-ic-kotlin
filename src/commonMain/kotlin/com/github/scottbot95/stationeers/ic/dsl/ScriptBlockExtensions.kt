package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueDelegateProvider

// #region Delegate extensions

inline fun ScriptBlock.registers(init: AliasedScriptValueContainer<Register>.() -> Unit): AliasedScriptValueContainer<Register> {
    registers.init()
    return registers
}

inline val ScriptBlock.register: AliasedScriptValueDelegateProvider<Register> get() = register()

fun ScriptBlock.register(name: String): AliasedScriptValueDelegateProvider<Register> = register(null, name)

fun ScriptBlock.register(
    register: Register? = null,
    name: String? = null
): AliasedScriptValueDelegateProvider<Register> {
    return AliasedScriptValueDelegateProvider(registers, register, name)
}

inline fun ScriptBlock.devices(init: AliasedScriptValueContainer<Device>.() -> Unit): AliasedScriptValueContainer<Device> {
    devices.init()
    return devices
}

inline val ScriptBlock.device: AliasedScriptValueDelegateProvider<Device> get() = device()

fun ScriptBlock.device(name: String): AliasedScriptValueDelegateProvider<Device> = device(null, name)

fun ScriptBlock.device(
    device: Device? = null,
    name: String? = null
): AliasedScriptValueDelegateProvider<Device> {
    return AliasedScriptValueDelegateProvider(devices, device, name)
}

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

fun ScriptBlock.readDevice(output: ScriptValue<Register>, device: ScriptValue<Device>, deviceVar: String) {
    +Operation.Load(output, device, deviceVar)
}

fun ScriptBlock.writeDevice(device: ScriptValue<Device>, deviceVar: String, value: ScriptValue<*>) {
    +Operation.Save(device, deviceVar, value)
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
