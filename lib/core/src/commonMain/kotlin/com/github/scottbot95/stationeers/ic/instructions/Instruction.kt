package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.CompileContext
import com.github.scottbot95.stationeers.ic.ICScriptStatement
import com.github.scottbot95.stationeers.ic.ScriptValue

sealed class Instruction(
    val opCode: String,
    vararg val args: ScriptValue<*>
) : ICScriptStatement {
    override fun render(context: CompileContext): String {
        val argString = args.joinToString(" ") { it.compile(context).toString() }
        return "$opCode $argString".trim()
    }
}
