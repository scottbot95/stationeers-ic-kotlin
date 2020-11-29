package com.github.scottbot95.stationeers.ic.simulation

import com.github.scottbot95.stationeers.ic.Register

typealias RegisterValues = Map<Register, Number>

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
    val registers: RegisterValues = Register.values().map { it to 0 }.toMap(),
)

/**
 * Advances [SimulationState.instructionPointer] and updates registers to provided values.
 * Make a copy of [SimulationState.registers] by default
 */
fun SimulationState.next(newRegisters: RegisterValues = registers.toMap(), nextIP: Int = instructionPointer + 1) = copy(
    instructionPointer = nextIP,
    registers = newRegisters
)

/**
 * Advances [SimulationState.instructionPointer] and overwrites [Register]s with updates from [regUpdates].
 */
fun SimulationState.next(vararg regUpdates: Pair<Register, Number>, nextIP: Int = instructionPointer + 1) =
    next(
        registers.toMutableMap().apply {
            putAll(regUpdates)
        },
        nextIP
    )
