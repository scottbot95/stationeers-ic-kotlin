package com.github.scottbot95.stationeers.ic.dsl

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

data class CompiledLine(val parts: List<ScriptValue<*>>) : ScriptValue<String> by CombinedScriptValue(parts) {
    constructor(vararg parts: ScriptValue<*>) : this(parts.toList())
    constructor(vararg parts: String) : this(parts.map(ScriptValue.Companion::of).toList())
}

data class CompileResults(val startContext: CompileContext, val lines: List<CompiledLine>) {

    constructor(context: CompileContext) : this(context, listOf())
    constructor(context: CompileContext, vararg lines: String) : this(context, lines.map { CompiledLine(it) })
    constructor(context: CompileContext, vararg lines: CompiledLine) : this(context, lines.asList())

    val endContext by lazy { startContext + lines.size }
    val asString by lazy {
        lines.mapIndexed { i, it -> it.toString(startContext + i) }
            .joinToString("\n")
    }
    val size by lazy { asString.length * 2 } // 2 bytes per character
}

/**
 * Appends the [lines] from [other] to [this].
 * Keeps all other values from [this] NOTE: A + B != B + A
 */
operator fun CompileResults.plus(other: CompileResults): CompileResults =
    this.copy(
        lines = this.lines + other.lines,
        startContext = this.startContext + other.startContext
    )

fun CompileResults.withLines(lines: List<CompiledLine>) = this.copy(lines = lines)

operator fun CompileContext.plus(lines: Int): CompileContext = this.copy(startLine = this.startLine + lines)
operator fun CompileContext.plus(other: CompileContext): CompileContext = this

/**
 * An object that can be compiled
 */
fun interface Compilable {
    fun compile(context: CompileContext): CompileResults

    /**
     * Object representing the empty [Compilable]. Will always return a blank [CompileResults]
     */
    object Empty : Compilable {
        override fun compile(context: CompileContext): CompileResults = CompileResults(context)
    }
}

// TODO if it ever becomes feasible make numLines a max spacing
//  Probably would require context to include the running results
class Spacer(private val numLines: Int) : Compilable {
    override fun compile(context: CompileContext): CompileResults {
        // Don't put spacers at start of file. Should we really do this...?
        return if (context.compileOptions.minify || context.startLine == 0) {
            CompileResults(context)
        } else {
            CompileResults(context, lines = List(numLines) { CompiledLine("") })
        }
    }
}

fun Compilable.compile(options: CompileOptions): CompileResults = compile(CompileContext(compileOptions = options))
