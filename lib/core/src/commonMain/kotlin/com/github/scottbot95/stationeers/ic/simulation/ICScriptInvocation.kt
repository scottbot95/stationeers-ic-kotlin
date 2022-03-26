package com.github.scottbot95.stationeers.ic.simulation

interface ICScriptInvocation {

    /**
     * Current [SimulationState] of the invocation
     */
    val state: SimulationState

    /**
     * Step the simulation once
     *
     * @return The result of this step
     */
    fun step(): StepResult
}

/**
 * Run the simulation for up to [maxSteps] steps
 *
 * @param maxSteps The max number of steps to run the simulation for or null for unlimited
 */
operator fun ICScriptInvocation.invoke(maxSteps: UInt? = null) {
    var step = 0U
    do {
        step++
        val result = step()
    } while (maxSteps != null && result !is StepResult.Error)
}
