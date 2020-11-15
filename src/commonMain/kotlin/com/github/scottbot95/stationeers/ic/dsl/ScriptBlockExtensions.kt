package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.RegisterValue
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueDelegateProvider

// #region Delegate extensions

inline fun ScriptBlock.registers(init: RegisterContainer.() -> Unit): RegisterContainer {
    registers.init()
    return registers
}

inline val ScriptBlock.register: AliasedScriptValueDelegateProvider<Register> get() = register()

inline fun ScriptBlock.register(name: String): AliasedScriptValueDelegateProvider<Register> = register(null, name)

fun ScriptBlock.register(
    register: Register? = null,
    name: String? = null
): AliasedScriptValueDelegateProvider<Register> {
    return AliasedScriptValueDelegateProvider(registers, register, name)
}

inline fun ScriptBlock.devices(init: DeviceContainer.() -> Unit): DeviceContainer {
    devices.init()
    return devices
}

inline val ScriptBlock.device: AliasedScriptValueDelegateProvider<Device> get() = device()

inline fun ScriptBlock.device(name: String): AliasedScriptValueDelegateProvider<Device> = device(null, name)

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

fun ScriptBlock.add(output: RegisterValue, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Add(output, a, b)
}

// #endregion

// #region Composite Operation extensions

// TODO Make a named subclass to handle this
inline fun forever(label: String? = null, init: ScriptBlock.() -> Unit): ScriptBlock = LoopingScriptBlock(label).apply(init)

// #endregion
