package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.simulation.CompiledScript
import com.github.scottbot95.stationeers.ic.util.OperationList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class PartialCompiledScript constructor(
    override val compileOptions: CompileOptions,
    override val operations: PersistentList<CompiledOperation>
) : OperationList(compileOptions, operations) {

    val nextLine get() = numLines

    fun addOperation(operation: CompiledOperation) = copy(operations = operations.add(operation))

    fun addOperations(newOperations: Collection<CompiledOperation>) =
        copy(operations = operations.addAll(newOperations))

    fun builder(): Builder = Builder(compileOptions, operations.builder())

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

        fun build(): PartialCompiledScript = PartialCompiledScript(options, operations.toPersistentList())
    }

    override fun toString() = super.toString()
}

// **************************************
// Extensions file
// **************************************

operator fun PartialCompiledScript.plus(operation: CompiledOperation) = addOperation(operation)
operator fun PartialCompiledScript.plus(newOperations: Collection<CompiledOperation>) = addOperations(newOperations)

fun PartialCompiledScript.toCompiledScript() = CompiledScript(compileOptions, operations)
