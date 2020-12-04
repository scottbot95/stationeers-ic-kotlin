package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation

fun interface Compilable2 {
    fun compile2(compiledScript: CompiledScript): CompiledScript

    object Empty : Compilable2 {
        override fun compile2(compiledScript: CompiledScript) = compiledScript
    }

    object Noop : Compilable2 {
        override fun compile2(compiledScript: CompiledScript) = compiledScript + CompiledOperation.Noop
    }
}
