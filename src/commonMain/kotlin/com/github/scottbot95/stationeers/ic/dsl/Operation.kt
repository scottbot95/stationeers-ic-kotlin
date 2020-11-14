package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.RegisterValue

/**
 * Generic class representing an operation within the MIPS language
 */
sealed class Operation(val opCode: String, vararg val args: ScriptValue<*>) : Compilable {

    data class Add(val output: RegisterValue, val a: ScriptValue<*>, val b: ScriptValue<*>) : Operation("add", output, a, b)
    data class Comment(val message: String) : Operation("#", StringScriptValue(message))

    override fun compile(options: CompileOptions): CompileResults {
        val combinedArgs = args.joinToString(" ") { it.toString(options) }
        return CompileResults(lines = listOf("$opCode $combinedArgs"))
    }
}

fun ScriptBlock.comment(message: String) {
    +Operation.Comment(message)
}

fun ScriptBlock.add(output: RegisterValue, a: ScriptValue<*>, b: ScriptValue<*>) {
    +Operation.Add(output, a, b)
}
