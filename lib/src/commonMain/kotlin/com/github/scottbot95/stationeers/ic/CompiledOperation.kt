package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.simulation.SimulationState

/**
 * Something that can be simulated
 */
fun interface Statement {
    /**
     * Simulates this [Statement] against [SimulationState] and returns the new [SimulationState] object.
     * Most likely, this should end with a call to [SimulationState.next]
     */
    operator fun invoke(state: SimulationState): SimulationState

    companion object {
        val Noop = Statement { it.next() }
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
