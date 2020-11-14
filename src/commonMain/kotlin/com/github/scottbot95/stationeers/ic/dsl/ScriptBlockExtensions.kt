package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register

inline fun ScriptBlock.block(init: ScriptBlock.() -> Unit): ScriptBlock {
    val block = SimpleScriptBlock(this)
    block.init()
    +block
    return block
}

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
