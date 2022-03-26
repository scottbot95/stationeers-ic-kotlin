package com.github.scottbot95.stationeers.ic

data class CompileOptions(
    val minify: Boolean = false,
)

data class CompileContext(
    val options: CompileOptions,
    val nextLineNum: Int,
)

interface CompiledICScriptBuilder {
    val context: CompileContext

    /**
     * Add a blank line to the exported script
     *
     * @return <code>this</code>
     */
    fun appendLine(): CompiledICScriptBuilder

    /**
     * Add [statement] to the compiled [ICScript]
     *
     * @param statement The [ICScriptStatement] to add to the script
     * @return <code>this</code>
     */
    fun appendLine(statement: ICScriptStatement): CompiledICScriptBuilder

    /**
     * Build this script into an [ICScript] instance
     */
    fun build(): ICScript
}

class StandardCompiledICScriptBuilder(private val options: CompileOptions) : CompiledICScriptBuilder {
    override val context: CompileContext
        get() = CompileContext(options, _statements.size + 1)

    private val _statements: MutableList<ICScriptStatement> = mutableListOf()

    override fun appendLine(): CompiledICScriptBuilder {
        _statements.add(ICScriptStatement.EMPTY)
        return this
    }

    override fun appendLine(statement: ICScriptStatement): CompiledICScriptBuilder {
        _statements.add(statement)
        return this
    }

    override fun build(): ICScript {
        return object : ICScript {
            override val statements: List<ICScriptStatement> = _statements
        }
    }
}

fun CompiledICScriptBuilder.appendLine(block: (CompileContext) -> ICScriptStatement): CompiledICScriptBuilder {
    val statement = block(context)
    return appendLine(statement)
}
