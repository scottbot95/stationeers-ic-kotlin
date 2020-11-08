package com.github.scottbot95.stationeers.ic.dsl

data class CompileOptions(val minify: Boolean = false)

data class CompileResults(val lines: List<String>) {
    val asString by lazy { lines.joinToString("\n") }
    val size by lazy { asString.length * 2 }
}

operator fun CompileResults.plus(other: CompileResults): CompileResults = this.copy(lines = this.lines + other.lines)

interface Compilable {
    fun compile(options: CompileOptions): CompileResults
}

