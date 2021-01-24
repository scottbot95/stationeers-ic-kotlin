package com.github.scottbot95.stationeers.ic.simulation

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.ISimulationResults
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.SimulationResults
import com.github.scottbot95.stationeers.ic.devices.LogicDeviceVar
import com.github.scottbot95.stationeers.ic.dsl.CompileOptions
import com.github.scottbot95.stationeers.ic.util.OperationList
import com.github.scottbot95.stationeers.ic.util.ValueTransformer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

data class TimedSimulationResults(val results: ISimulationResults, val time: Double) : ISimulationResults by results

data class MultiSimulationResults(
    val results: List<TimedSimulationResults>,
    val totalTime: Double
) : ISimulationResults by results.last()

@OptIn(ExperimentalTime::class, ICSimulation::class)
class CompiledScript(
    compileOptions: CompileOptions,
    operations: List<CompiledOperation>,
) : OperationList(compileOptions, operations) {

    // TODO we can probably somehow use contracts to remove lateinit since we're calling reset in the init block
    private lateinit var currState: SimulationState

    init {
        reset()
    }

    @ICSimulation
    fun reset(initialState: SimulationState = SimulationState.Initial) {
        currState = initialState
    }

    @ICSimulation
    fun step(): TimedSimulationResults {
        val results: ISimulationResults
        val time = measureTime {
            results = stepInternal()
        }

        return TimedSimulationResults(results, time.inMilliseconds)
    }

    @ICSimulation
    fun run(): MultiSimulationResults {
        val totalResults = mutableListOf<TimedSimulationResults>()

        val totalTime = measureTime {
            do {
                val stepResults = step()
                totalResults += stepResults

                val status = stepResults.status
            } while (status.isSuccess && status != SimulationStatus.Yielded)
        }

        return MultiSimulationResults(totalResults, totalTime.inMilliseconds)
    }

    @ICSimulation
    fun runToEnd(): MultiSimulationResults {
        val totalResults = mutableListOf<TimedSimulationResults>()

        val totalTime = measureTime {
            do {
                val tickResults = run()
                totalResults.addAll(tickResults.results)
            } while (tickResults.status.isSuccess)
        }

        return MultiSimulationResults(totalResults, totalTime.inMilliseconds)
    }

    @JvmName("addRegisterTransformer")
    fun addTransformer(transformer: ValueTransformer<Register, Double>): CompiledScript {
        currState += transformer
        return this
    }

    @JvmName("addDeviceTransformer")
    fun addTransformer(transformer: ValueTransformer<LogicDeviceVar, Double>): CompiledScript {
        currState += transformer
        return this
    }

    private fun stepInternal(): ISimulationResults {
        val nextOp = if (currState.instructionPointer < operations.size) {
            operations[currState.instructionPointer]
        } else {
            return SimulationResults(currState, SimulationStatus.Finished)
        }

        val results = try {
            nextOp.invoke(currState)
        } catch (e: SimulationException) {
            return SimulationResults(currState, SimulationStatus.Errored(e.message, e.cause))
        }

        currState = results.endState

        return if (currState.instructionPointer >= operations.size) {
            SimulationResults(results.endState, SimulationStatus.Finished)
        } else {
            results
        }
    }
}
