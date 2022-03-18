package com.github.scottbot95.stationeers.ic.simulation

sealed class StepResult {
    object Success: StepResult()
    object Yield: StepResult()
    class Error(val message: String): StepResult()
}
