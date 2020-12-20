package com.github.scottbot95.stationeers.ic.simulation

sealed class SimulationStatus(val isSuccess: Boolean = true) {

    // Do we really need Initialized or can it just start as Yielded?
    object Initialized : SimulationStatus()
    object Running : SimulationStatus()
    object Yielded : SimulationStatus()
    object Finished : SimulationStatus()

    /**
     * Indicates an error occurred while simulating. In most cases, you do no need to return this directly,
     * but instead should just throw a [SimulationException]
     */
    data class Errored(val message: String, val cause: Throwable?) : SimulationStatus(false)
}
