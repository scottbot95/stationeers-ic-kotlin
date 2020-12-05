package com.github.scottbot95.stationeers.ic.simulation

fun interface SimulationChangeHandler<in K, V : Any> {
    /**
     * Handle the request to change [key] from [oldValue] to [newValue].
     * Function should return the validated [newValue] (for instance ensuring that [newValue] is positive)
     */
    operator fun invoke(key: K, oldValue: V, newValue: V): V
}
