package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileOptions

abstract class OperationList(
    open val compileOptions: CompileOptions,
    open val operations: List<CompiledOperation>,
) {
    private val OperationList.asString by lazy {
        operations.asSequence()
            .mapIndexed { i, it -> it.values.toString(CompileContext(i, compileOptions)) }
            .joinToString("\n")
    }

    val size get() = asString.length * 2 // 2 bytes per character
    val numLines get() = operations.size

    override fun toString(): String = asString
}
