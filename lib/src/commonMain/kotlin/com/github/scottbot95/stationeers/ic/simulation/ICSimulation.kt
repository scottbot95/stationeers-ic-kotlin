package com.github.scottbot95.stationeers.ic.simulation

@RequiresOptIn(message = "IC Simulation is currently experimental. The simulation results may not be accurate and the API may change without notice.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ICSimulation
