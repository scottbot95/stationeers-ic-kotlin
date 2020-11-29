package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import com.github.scottbot95.stationeers.ic.simulation.next

/**
 * Something that can be simulated
 */
fun interface Simulator {
    /**
     * Simulates this operation and returns a new [SimulationState] object.
     * Most likely, this should end with a call to [SimulationState.next]
     */
    fun simulate(state: SimulationState): SimulationState
}

fun interface Evaluator<out T> {
    fun evaluate(state: SimulationState): T
}

/**
 * An [Operation] that has been compiled
 */
open class CompiledOperation(private val stringValue: String, simulated: Simulator) : Simulator by simulated {
    override fun toString(): String = stringValue

    /**
     * Noop blank line
     */
    object Noop : CompiledOperation("", { it.next() })
}
