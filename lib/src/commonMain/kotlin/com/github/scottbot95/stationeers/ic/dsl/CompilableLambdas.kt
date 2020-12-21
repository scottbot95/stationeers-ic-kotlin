package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.simulation.CompiledScript

inline fun Compilable.compile(init: CompileOptions.Builder.() -> Unit): CompiledScript {
    val options = CompileOptions.Builder().apply(init).build()
    return compile(options)
}

inline fun compileOptions(init: CompileOptions.Builder.() -> Unit): CompileOptions {
    return CompileOptions.Builder().apply(init).build()
}
