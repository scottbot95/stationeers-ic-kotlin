package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.JumpType
import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.devices.LogicDevice
import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueDelegateProvider
import com.github.scottbot95.stationeers.ic.util.Conditional
import com.github.scottbot95.stationeers.ic.util.ConstantReadOnlyProperty
import com.github.scottbot95.stationeers.ic.util.DefaultAliasedScriptValueDelegateProvider
import com.github.scottbot95.stationeers.ic.util.LabelDelegateProvider
import com.github.scottbot95.stationeers.ic.util.combine
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

//region Delegate extensions

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
    deviceConstructor: (alias: String?, device: ScriptValue<Device>) -> T,
    device: Device? = null,
    name: String? = null,
): AliasedScriptValueDelegateProvider<Device, T> {
    // TODO not sure I like the lambda here for ignoring a function arg...
    return AliasedScriptValueDelegateProvider(
        devices,
        device,
        name
    ) { alias: String?, deviceValue: ScriptValue<Device>, _: () -> Unit -> deviceConstructor(alias, deviceValue) }
}

fun ScriptBlock.define(name: String, value: Double): ScriptValue<Double> {
    +Operation.Define(name, value)
    return SimpleAliasedScriptValue(name, ScriptValue.of(value))
}

class DefineDelegateProvider(
    private val block: ScriptBlock,
    private val value: Double,
    private val name: String? = null
) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, ScriptValue<Double>> =
        ConstantReadOnlyProperty(block.define(name ?: prop.name, value))
}

fun ScriptBlock.define(value: Double, name: String? = null) = DefineDelegateProvider(this, value, name)

//endregion

//region Basic Operation extensions

inline fun ScriptBlock.block(spacing: Int = 1, init: ScriptBlock.() -> Unit): ScriptBlock {
    val block = SimpleScriptBlock(this, spacing)
    block.init()
    +block
    return block
}

fun ScriptBlock.comment(message: String, spacing: Int = 1) {
    spacing(spacing)
    +Operation.Comment(message)
}

fun ScriptBlock.add(output: ScriptValue<Register>, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Add(output, a, b)
}

fun ScriptBlock.subtract(output: ScriptValue<Register>, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Subtract(output, a, b)
}

fun ScriptBlock.max(output: ScriptValue<Register>, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Max(output, a, b)
}

fun ScriptBlock.min(output: ScriptValue<Register>, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Min(output, a, b)
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

fun ScriptBlock.jump(target: LineReference, function: Boolean = false) {
    +Operation.Jump(target, if (function) JumpType.FUNCTION else null)
}

fun ScriptBlock.branch(condition: Conditional, target: LineReference, function: Boolean = false) {
    +Operation.Branch(condition, target, setOfNotNull(if (function) JumpType.FUNCTION else null))
}

fun ScriptBlock.yield() {
    +Operation.Yield()
}

fun ScriptBlock.move(register: ScriptValue<Register>, value: ScriptValue<*>) {
    +Operation.Move(register, value)
}

fun ScriptBlock.spacing(size: Int) {
    +Spacer(size)
}

//endregion

//region Composite Operation extensions

inline fun ScriptBlock.forever(
    label: String? = null,
    spacing: Int = 1,
    init: LoopingScriptBlock.() -> Unit
): LoopingScriptBlock =
    LoopingScriptBlock(LoopOptions(label = label, spacing = spacing), this).also {
        it.init()
        +it
    }

inline fun ScriptBlock.loop(
    options: LoopOptions,
    init: LoopingScriptBlock.() -> Unit
): LoopingScriptBlock = LoopingScriptBlock(options, this).also {
    it.init()
    +it
}

// Maybe this should be inline?
fun ScriptBlock.cond(
    vararg branches: Pair<Conditional, ScriptBlock.() -> Unit>,
) {
    val branchSequence = branches.asSequence().map { it.first to SimpleScriptBlock(this).apply(it.second) }
    // .firstOrNull {it.first !== Conditional.None } doesn't return nullable for some reason. Do this instead
    val default = branchSequence.filter { it.first === Conditional.None }.map { it.second }.firstOrNull()
    val conditions = branchSequence.filter { it.first !== Conditional.None }.toList()

    block(0) {
        conditions.forEach {
            branch(it.first, it.second.start)
        }
        val branchBlocks = listOfNotNull(default) + conditions.map { it.second }
        +branchBlocks.combine(Operation.Jump(end))

        // old implementation. Technically one line shorter in cases without a default case
//        conditions.forEachIndexed { i, (condition, condBlock) ->
//            val nextBranch = FixedLineReference()
//            branch(condition.inverse, nextBranch)
//            condBlock()
//            if (i != branches.size - 1) jump(end)
//            +nextBranch.inject
//        }
//
//        if (default != null) {
//            default()
//        }
    }
}

// TODO can we somehow wire these to ++ and -- on a register?
fun ScriptBlock.inc(register: ScriptValue<Register>, increment: ScriptValue<*> = ScriptValue.of(1.0)) {
    +Operation.Add(register, register, increment)
}

fun ScriptBlock.dec(register: ScriptValue<Register>, increment: ScriptValue<*> = ScriptValue.of(1.0)) {
    +Operation.Subtract(register, register, increment)
}

/**
 * Creates a label [label] or the property name if [label] is null. Optionally automatically injects the [LineReference]
 * NOTE: The label will **not** be created if not being used as a delegate (eg just calling [ScriptBlock.label] directly)
 */
fun ScriptBlock.label(label: String? = null, inject: Boolean = true) = LabelDelegateProvider(this, label, inject)

fun ScriptBlock.reference(label: String? = null, inject: Boolean = true): LineReference =
    FixedLineReference(label).also {
        if (label != null) labels.add(it)
        if (inject) +it.inject
    }

//endregion
