package com.github.scottbot95.stationeers.ic

open class StandardCodeBlockBuilder<T : CodeBlockBuilder<T>> : CodeBlockBuilder<T> {
    val entries: MutableList<Compilable> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    override fun appendEntry(entry: Compilable): T {
        entries.add(entry)
        return this as T
    }
}

interface ICScriptBuilder : CodeBlockBuilder<ICScriptBuilder> {

    fun compile(options: CompileOptions): ICScript

    fun newCodeBlock(): CodeBlock

    companion object {
        fun standard(): ICScriptBuilder = object : StandardCodeBlockBuilder<ICScriptBuilder>(), ICScriptBuilder {

            override fun compile(options: CompileOptions): ICScript {
                val compiledBuilder = StandardCompiledICScriptBuilder(options)
                entries.forEach {
                    it.compile(compiledBuilder)
                }
                return compiledBuilder.build()
            }

            // Should this go somewhere else?
            override fun newCodeBlock(): CodeBlock = object : StandardCodeBlockBuilder<CodeBlock>(), CodeBlock {
                override fun compile(builder: CompiledICScriptBuilder) {
                    entries.forEach {
                        it.compile(builder)
                    }
                }
            }
        }
    }
}
