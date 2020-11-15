package com.github.scottbot95.stationeers.ic.dsl

data class CompileOptions(
    val minify: Boolean = false,
    val preferRelativeJumps: Boolean = false,
)

data class CompileResults(val lines: List<String>) {
    constructor(vararg lines: String) : this(lines.asList())

    val asString by lazy { lines.joinToString("\n") }
    val size by lazy { asString.length * 2 } // 2 bytes per character
}

inline operator fun CompileResults.plus(other: CompileResults): CompileResults =
    this.copy(lines = this.lines + other.lines)

inline fun CompileResults.withLines(lines: List<String>): CompileResults = this.copy(lines = lines)

data class CompileContext(val startLine: Int = 0)

inline operator fun CompileContext.plus(lines: Int): CompileContext = this.copy(startLine = this.startLine + lines)

fun interface Compilable {
    fun compile(options: CompileOptions, context: CompileContext): CompileResults
}
