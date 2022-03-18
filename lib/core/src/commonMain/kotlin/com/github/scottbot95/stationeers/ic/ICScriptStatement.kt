package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.simulation.ICScriptRuntimeException
import com.github.scottbot95.stationeers.ic.simulation.SimulationState

interface ICScriptStatement {

    /**
     * Invoke this [ICScriptStatement] against the given [SimulationState], returning the next simulation state
     *
     * @param state The current state of the simulation
     *
     * @return The new state of the simulation (may be original state if statement is a no-op)
     * @throws [ICScriptRuntimeException] if an execution error occurs (includes yields)
     */
    @Throws(ICScriptRuntimeException::class)
    operator fun invoke(state: SimulationState): SimulationState

    /**
     * Render this statement to a string (should *not* include newline character)
     */
    fun toString(options: ExportOptions): String
}
