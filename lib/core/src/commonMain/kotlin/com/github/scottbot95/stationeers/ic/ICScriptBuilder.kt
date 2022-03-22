package com.github.scottbot95.stationeers.ic

//data class CompileOptions(
//    val ignoreErrors: Boolean = false
//)

interface ICScriptBuilderEntry {
    fun compile(context: CompileContext): ICScriptStatement
}

interface ICScriptBuilder {

    fun appendEntry(entry: ICScriptBuilderEntry): ICScriptBuilder

    fun compile(options: CompileOptions): ICScript
}