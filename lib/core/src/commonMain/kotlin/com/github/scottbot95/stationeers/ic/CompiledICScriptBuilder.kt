package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Instruction

interface CompiledICScriptBuilder {
    val context: CompileContext

    /**
     * Add a blank line to the exported script
     *
     * @return <code>this</code>
     */
    fun appendLine(): CompiledICScriptBuilder

    /**
     * Add [string] to the exported script
     *
     * @param string The [String] to add to the script
     * @return <code>this</code>
     */
    fun appendLine(statement: ICScriptStatement): CompiledICScriptBuilder

    /**
     * Append specified [Instruction] to the compiled script
     */
    fun appendInstruction(instruction: Instruction): CompiledICScriptBuilder

    /**
     * Final export to [String]
     */
    override fun toString(): String
}
