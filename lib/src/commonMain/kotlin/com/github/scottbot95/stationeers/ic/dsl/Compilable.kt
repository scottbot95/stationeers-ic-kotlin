package com.github.scottbot95.stationeers.ic.dsl

data class CompileOptions(
    var minify: Boolean = false,
    var preferRelativeJumps: Boolean = false,
)

data class ExportOptions(
    var compileOptions: CompileOptions,
    var destination: String,
)

data class CompileContext(
    val startLine: Int = 0,
    val compileOptions: CompileOptions = CompileOptions(),
    val labels: Map<String, Int> = mapOf()
)

data class CompileResults(val startContext: CompileContext, val lines: List<String>) {

    constructor(context: CompileContext, vararg lines: String) : this(context, lines.asList())

    val endContext by lazy { startContext + lines.size }
    val asString by lazy { lines.joinToString("\n") }
    val size by lazy { asString.length * 2 } // 2 bytes per character
}

/**
 * Appends the [lines] from [other] to [this] and merges the values of [CompileContext.labels] from [CompileResults.startContext].
 * Keeps all other values from [this] NOTE: A + B != B + A
 */
operator fun CompileResults.plus(other: CompileResults): CompileResults =
    this.copy(
        lines = this.lines + other.lines,
        startContext = this.startContext + other.startContext
    )

fun CompileResults.withLines(lines: List<String>): CompileResults = this.copy(lines = lines)

operator fun CompileContext.plus(lines: Int): CompileContext = this.copy(startLine = this.startLine + lines)
operator fun CompileContext.plus(other: CompileContext): CompileContext = this.copy(labels = this.labels + other.labels)

fun interface Compilable {
    fun compile(context: CompileContext): CompileResults
}

fun Compilable.compile(options: CompileOptions): CompileResults = compile(CompileContext(compileOptions = options))

inline fun Compilable.compile(init: CompileOptions.() -> Unit): CompileResults {
    val options = CompileOptions().apply(init)
    return compile(options)
}
