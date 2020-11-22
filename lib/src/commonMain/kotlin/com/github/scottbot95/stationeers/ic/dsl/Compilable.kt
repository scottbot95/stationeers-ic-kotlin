package com.github.scottbot95.stationeers.ic.dsl

data class CompileOptions(
    var minify: Boolean = false,
    var preferRelativeJumps: Boolean = false,
)

data class ExportOptions(
    var compileOptions: CompileOptions,
    var destination: String,
)

data class CompileContext(val startLine: Int = 0)

data class CompileResults(val lines: List<String>) {

    constructor(vararg lines: String) : this(lines.asList())
    val asString by lazy { lines.joinToString("\n") }
    val size by lazy { asString.length * 2 } // 2 bytes per character
}

operator fun CompileResults.plus(other: CompileResults): CompileResults =
    this.copy(lines = this.lines + other.lines)

fun CompileResults.withLines(lines: List<String>): CompileResults = this.copy(lines = lines)

operator fun CompileContext.plus(lines: Int): CompileContext = this.copy(startLine = this.startLine + lines)

fun interface Compilable {
    fun compile(options: CompileOptions, context: CompileContext): CompileResults
}

fun Compilable.compile(options: CompileOptions): CompileResults = compile(options, CompileContext())

inline fun Compilable.compile(init: CompileOptions.() -> Unit): CompileResults {
    val options = CompileOptions().apply(init)
    return compile(options, CompileContext())
}
