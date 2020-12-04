package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.of
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
}

fun interface Expression<out T> {
    fun evaluate(state: SimulationState): T
}

/**
 * An [Operation] that has been compiled
 */
open class CompiledOperation(
    private val value: ScriptValue<String>,
    private val context: CompileContext = CompileContext(),
    statement: Statement
) : Statement by statement {

    override fun toString(): String = value.toString(context)

    /**
     * Noop blank line
     */
    object Noop : CompiledOperation(ScriptValue.of(""), statement = { it.next() })
}
