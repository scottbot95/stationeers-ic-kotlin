package com.github.scottbot95.stationeers.ic

interface ICScriptStatement {

    fun render(context: CompileContext): String

//    /**
//     * Invoke this [ICScriptStatement] against the given [SimulationState], returning the next simulation state
//     *
//     * @param state The current state of the simulation
//     *
//     * @return The new state of the simulation (may be original state if statement is a no-op)
//     * @throws [ICScriptRuntimeException] if an execution error occurs (includes yields)
//     */
//    @Throws(ICScriptRuntimeException::class)
//    // TODO I kinda want this to be separate so we can decouple the script building and simulations
//    operator fun invoke(state: SimulationState): SimulationState
}
