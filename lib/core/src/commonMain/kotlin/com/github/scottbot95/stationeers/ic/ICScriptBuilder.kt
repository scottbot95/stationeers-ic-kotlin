package com.github.scottbot95.stationeers.ic

data class CompileOptions(
    val ignoreErrors: Boolean = false
)

interface ICScriptBuilder {
    fun compile(options: CompileOptions): ICScript
}