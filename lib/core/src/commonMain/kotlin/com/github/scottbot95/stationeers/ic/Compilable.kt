package com.github.scottbot95.stationeers.ic

data class CompileOptions(
    val minify: Boolean = false,
)

data class CompileContext(
    val options: CompileOptions,
)

fun interface Compilable {
    fun compile(builder: CompiledICScriptBuilder)
}