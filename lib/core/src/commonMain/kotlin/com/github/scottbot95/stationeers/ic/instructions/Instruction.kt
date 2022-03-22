package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.CompileContext
import com.github.scottbot95.stationeers.ic.ICScriptBuilderEntry
import com.github.scottbot95.stationeers.ic.ICScriptStatement
import com.github.scottbot95.stationeers.ic.ScriptValue

sealed class Instruction(
    val opCode: String,
    vararg val args: ScriptValue
) : ICScriptBuilderEntry {
    override fun compile(context: CompileContext): ICScriptStatement {
        val argString = args.joinToString(" ") { it.render(context) }
        val compiled = "$opCode $argString".trim()
        return object : ICScriptStatement {
            override fun toString(): String = compiled
        }
    }
}
