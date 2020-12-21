package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import com.github.scottbot95.stationeers.ic.simulation.SimulationStatus

// Ewwww using the "I" prefix. Find something better
interface ISimulationResults {
    val endState: SimulationState
    val status: SimulationStatus
}

data class SimulationResults(
    override val endState: SimulationState,
    override val status: SimulationStatus = SimulationStatus.Running
) : ISimulationResults

/**
 * Something that can be simulated
 */
fun interface Statement {
    /**
     * Simulates this [Statement] against [SimulationState] and returns the new [SimulationState] object.
     * Most likely, this should end with a call to [SimulationState.next]
     */
    operator fun invoke(state: SimulationState): ISimulationResults

    companion object {
        //        val Noop = Statement { it.next() }
        val Noop = Statement { SimulationResults(it.next()) }
    }
}

fun interface Expression<out T> {
    fun evaluate(state: SimulationState): T
}

/**
 * An [Operation] that has been compiled
 */
open class CompiledOperation(
    vararg val values: ScriptValue<*>,
    statement: Statement
) : Statement by statement {
    /**
     * Noop blank line
     */
    object Noop : CompiledOperation(ScriptValue.EMPTY, statement = Statement.Noop)
}
