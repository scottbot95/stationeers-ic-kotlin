package com.github.scottbot95.stationeers.ic.simulation

class SimulationException(
    val state: SimulationState, // TODO Maybe don't put this here...?
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)