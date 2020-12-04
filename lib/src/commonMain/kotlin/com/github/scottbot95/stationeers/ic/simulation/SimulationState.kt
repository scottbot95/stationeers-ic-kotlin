package com.github.scottbot95.stationeers.ic.simulation

import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.jvm.JvmStatic

typealias RegisterValues = PersistentMap<Register, Number>
typealias RegisterChangeListener = (register: Register, oldValue: Number, newValue: Number) -> Boolean
typealias DeviceChangeListener = (deviceVar: LogicDeviceVar, oldValue: Number, newValue: Number) -> Boolean

/**
 * Current state of the simulation
 */
data class SimulationState(
    /**
     * Pointer to the currently executing instruction
     */
    val instructionPointer: Int = 0,
    /**
     * [Map] of the registers and their current value. All registers default to 0
     */
    val registers: RegisterValues = persistentMapOf(*Register.values().map { it to 0 }.toTypedArray()),
    /**
     * Change listeners for changes to [registers].
     */
    private val registerListeners: List<RegisterChangeListener> = listOf(),
    /**
     * Change listeners for changes to [registers].
     */
    private val deviceListeners: List<RegisterChangeListener> = listOf(),
) {

    fun next(nextIP: Int = instructionPointer + 1) = copy(instructionPointer = nextIP)

    /**
     * Creates a new simulation state advancing the [instructionPointer] to [nextIP]
     *
     * If one or more [registerListeners] return false, the change will be ignored, but the [instructionPointer]
     * will still advance.
     */
    fun next(register: Register, newValue: Number, nextIP: Int = instructionPointer + 1): SimulationState {
        val keepChanges = registerListeners
            .asSequence()
            .map { it(register, registers.getValue(register), newValue) }
            .reduce { acc, it -> acc && it }

        return copy(
            instructionPointer = nextIP,
            registers = if (keepChanges) registers.put(register, newValue) else registers
        )
    }

    companion object {
        @JvmStatic
        val Initial = SimulationState()
    }
}
