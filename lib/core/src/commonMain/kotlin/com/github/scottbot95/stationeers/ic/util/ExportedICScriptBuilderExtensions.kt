package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.CompiledICScriptBuilder
import com.github.scottbot95.stationeers.ic.ScriptValue

fun CompiledICScriptBuilder.appendStatement(opCode: String, vararg args: ScriptValue): CompiledICScriptBuilder {
    val argsString = args.joinToString(" ") { it.render(context) }
    return appendLine("$opCode $argsString".trimEnd())
}