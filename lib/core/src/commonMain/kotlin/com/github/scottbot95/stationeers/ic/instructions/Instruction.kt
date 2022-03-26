package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.Compilable
import com.github.scottbot95.stationeers.ic.CompileContext
import com.github.scottbot95.stationeers.ic.CompiledICScriptBuilder
import com.github.scottbot95.stationeers.ic.ICScriptStatement
import com.github.scottbot95.stationeers.ic.ScriptValue
import com.github.scottbot95.stationeers.ic.appendLine

sealed class Instruction(
    val opCode: String,
    vararg val args: ScriptValue<*>
) : Compilable {
    override fun compile(builder: CompiledICScriptBuilder) {
        builder.appendLine(::Statement)
    }

    private inner class Statement(private val context: CompileContext) : ICScriptStatement {
        override val instruction: Instruction = this@Instruction

        override fun toString(): String {
            val argString = args.joinToString(" ") { it.compile(context).toString() }
            return "$opCode $argString".trim()
        }
    }
}
