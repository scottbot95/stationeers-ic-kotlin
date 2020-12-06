package com.github.scottbot95.stationeers.ic.dsl

inline fun Compilable.compile(init: CompileOptions.Builder.() -> Unit): CompileResults {
    val options = CompileOptions.Builder().apply(init).build()
    return compile(options)
}

inline fun compileOptions(init: CompileOptions.Builder.() -> Unit): CompileOptions {
    return CompileOptions.Builder().apply(init).build()
}
