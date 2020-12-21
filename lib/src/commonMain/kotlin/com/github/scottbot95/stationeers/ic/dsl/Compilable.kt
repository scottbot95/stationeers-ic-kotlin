package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.simulation.CompiledScript

data class CompileOptions(
    val minify: Boolean = false,
    val preferRelativeJumps: Boolean = false,
) {
    @ScriptDSL
    class Builder(
        var minify: Boolean = false,
        var preferRelativeJumps: Boolean = false
    ) {
        fun build(): CompileOptions = CompileOptions(
            minify,
            preferRelativeJumps
        )
    }
}

data class ExportOptions(
    var compileOptions: CompileOptions,
    var destination: String,
)

data class CompileContext(
    val startLine: Int = 0,
    val compileOptions: CompileOptions = CompileOptions(),
)

/**
 * An object that can be compiled
 */
fun interface Compilable {
    fun compile(partial: PartialCompiledScript): PartialCompiledScript

    object Empty : Compilable {
        override fun compile(partial: PartialCompiledScript) = partial
    }

    object Noop : Compilable {
        override fun compile(partial: PartialCompiledScript) = partial + CompiledOperation.Noop
    }
}

fun Compilable.compile(options: CompileOptions = CompileOptions()): CompiledScript =
    compile(PartialCompiledScript.empty(options)).toCompiledScript()
