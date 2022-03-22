package com.github.scottbot95.stationeers.ic

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
    fun appendLine(string: String): CompiledICScriptBuilder

    /**
     * Final export to [String]
     */
    override fun toString(): String
}
