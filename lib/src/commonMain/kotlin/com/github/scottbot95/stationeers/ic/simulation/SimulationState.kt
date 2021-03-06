package com.github.scottbot95.stationeers.ic.simulation

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
import com.github.scottbot95.stationeers.ic.util.UUID
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

typealias RegisterValues = PersistentMap<Register, Double>

typealias DeviceVarValues = PersistentMap<Device, PersistentMap<String, Double>>

private typealias RegisterChangeHandler = SimulationChangeHandler<Register, Double>
private typealias DeviceVarChangeHandler = SimulationChangeHandler<LogicDeviceVar, Double>

/**
 * Current state of the simulation
 */
class SimulationState private constructor(
    val runID: UUID = UUID.randomUUID(),
    /**
     * Pointer to the currently executing instruction
     */
    val instructionPointer: Int = 0,
    /**
     * [Map] of the registers and their current value. All registers default to 0
     */
    val registers: RegisterValues = persistentMapOf(),
    val devices: DeviceVarValues = persistentMapOf(),
    val executionCount: Int = 0,
) {

    /**
     * Creates a new simulation state advancing the [instructionPointer] to [nextIP]
     */
    // TODO I think I might want this to return a SimulationResult instead of another SimulationState...
    fun next(nextIP: Int = instructionPointer + 1) = copy(instructionPointer = nextIP)

    /**
     * Creates a new simulation state advancing the [instructionPointer] to [nextIP] and attempting to
     * update [register] to [newValue] validated against the [registerHandlers]
     */
    fun next(register: Register, newValue: Double, nextIP: Int = instructionPointer + 1): SimulationState {
        val currValue = registers[register] ?: 0.0
        val validatedValue = registerHandlers.validateChange(register, currValue, newValue)

        return copy(
            instructionPointer = nextIP,
            registers = registers.put(register, validatedValue)
        )
    }

    /**
     * Creates a new simulation state advancing the [instructionPointer] to [nextIP] and attempting to update
     * [deviceVar] to [newValue] validated against the [deviceVarHandlers]
     */
    fun next(deviceVar: LogicDeviceVar, newValue: Double, nextIP: Int = instructionPointer + 1): SimulationState {
        val device = deviceVar.device.value
        val varName = deviceVar.name

        val currValues = devices[device] ?: throw SimulationException(
            this,
            "Cannot read var '$varName' on disconnected device '$device'"
        )
        val currValue = currValues[varName] ?: 0.0
        val validatedValue = deviceVarHandlers.validateChange(deviceVar, currValue, newValue)
        return copy(
            instructionPointer = nextIP,
            devices = devices.put(device, currValues.put(varName, validatedValue))
        )
    }

    fun connect(device: Device, handler: ((oldValue: Double, newValue: Double) -> Double)? = null): SimulationState {
        if (handler != null) {
            deviceVarHandlers += DeviceVarChangeHandler { deviceVar: LogicDeviceVar, old: Double, new: Double ->
                if (deviceVar.device.value == device) handler(old, new) else new
            }
        }
        return copy(
            devices = devices.put(device, persistentMapOf())
        )
    }

    fun disconnect(device: Device): SimulationState = copy(
        devices = devices.remove(device)
    )

    // Make our own copy so we can control visibility and prevent changing [runId]
    private fun copy(
        instructionPointer: Int = this.instructionPointer,
        registers: RegisterValues = this.registers,
        devices: DeviceVarValues = this.devices
    ) = SimulationState(
        instructionPointer = instructionPointer,
        registers = registers,
        devices = devices,
        executionCount = executionCount + 1
    )

    companion object {
        @JvmStatic
        val Initial = SimulationState()
    }
}

fun <K, V : Any> List<SimulationChangeHandler<K, V>>.validateChange(key: K, oldValue: V, newValue: V): V {
    return fold(newValue) { acc, handler -> handler(key, oldValue, acc) }
}

private val REGISTER_HANDLER_MAP: MutableMap<UUID, MutableList<RegisterChangeHandler>> = mutableMapOf()

/**
 * Change listeners for changes to [SimulationState.registers].
 */
private inline val SimulationState.registerHandlers: MutableList<RegisterChangeHandler>
    get() = REGISTER_HANDLER_MAP.getOrPut(this.runID) { mutableListOf() }

@JvmName("addRegisterHandler")
operator fun SimulationState.plusAssign(handler: RegisterChangeHandler) {
    registerHandlers.add(handler)
}

@JvmName("removeRegisterHandler")
operator fun SimulationState.minusAssign(handler: RegisterChangeHandler) {
    registerHandlers.remove(handler)
}

private val DEVICE_VAR_HANDLER_MAP: MutableMap<UUID, MutableList<DeviceVarChangeHandler>> = mutableMapOf()

/**
 * Change listeners for changes to [SimulationState.registers].
 */
private inline val SimulationState.deviceVarHandlers: MutableList<DeviceVarChangeHandler>
    get() = DEVICE_VAR_HANDLER_MAP.getOrPut(this.runID) { mutableListOf() }

@JvmName("addDeviceVarHandler")
operator fun SimulationState.plusAssign(handler: DeviceVarChangeHandler) {
    deviceVarHandlers.add(handler)
}

@JvmName("removeDeviceVarHandler")
operator fun SimulationState.minusAssign(handler: DeviceVarChangeHandler) {
    deviceVarHandlers.remove(handler)
}
