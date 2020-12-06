package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class CompiledScript constructor(
    val options: CompileOptions,
    val operations: PersistentList<CompiledOperation>
) {
    private val asString by lazy { operations.joinToString() }

    val size get() = asString.length * 2 // 2 bytes per character
    val numLines by operations::size
    val nextLine get() = numLines + 1

    fun simulate(startState: SimulationState) =
        operations.fold(startState) { stepState, op -> op(stepState) }

    fun addOperation(operation: CompiledOperation) = copy(operations = operations.add(operation))

    fun addOperations(newOperations: Collection<CompiledOperation>) =
        copy(operations = operations.addAll(newOperations))

    fun builder(): Builder = Builder(options, operations.builder())

    override fun toString(): String = asString

    companion object {
        fun empty(options: CompileOptions = CompileOptions()) = Builder(options).build()
    }

    @ScriptDSL
    class Builder(
        var options: CompileOptions = CompileOptions(),
        private val operations: PersistentList.Builder<CompiledOperation> = persistentListOf<CompiledOperation>().builder()
    ) {

        operator fun CompiledOperation.unaryPlus() {
            addOperation(this)
        }

        operator fun Iterable<CompiledOperation>.unaryPlus() {
            addOperations(this)
        }

        fun addOperation(operation: CompiledOperation) = apply {
            operations.add(operation)
        }

        fun addOperations(operations: Iterable<CompiledOperation>) = apply {
            this.operations.addAll(operations)
        }

        fun build(): CompiledScript = CompiledScript(options, operations.toPersistentList())
    }
}

// **************************************
// Extensions file
// **************************************

operator fun CompiledScript.plus(operation: CompiledOperation) = addOperation(operation)
operator fun CompiledScript.plus(newOperations: Collection<CompiledOperation>) = addOperations(newOperations)
operator fun CompiledScript.invoke() = simulate(SimulationState.Initial)
