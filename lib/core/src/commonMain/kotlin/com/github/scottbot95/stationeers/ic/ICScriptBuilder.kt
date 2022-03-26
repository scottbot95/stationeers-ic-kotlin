package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Instruction

fun interface ICScriptBuilderEntry {
    fun compile(context: CompileContext): List<ICScriptStatement>
}

interface ICScriptBuilder {

    fun appendEntry(entry: ICScriptBuilderEntry): ICScriptBuilder

    fun compile(options: CompileOptions): ICScript

    companion object {
        fun standard(): ICScriptBuilder = object : ICScriptBuilder {
            private val entries: MutableList<ICScriptBuilderEntry> = mutableListOf()

            override fun appendEntry(entry: ICScriptBuilderEntry): ICScriptBuilder {
                entries.add(entry)
                return this
            }

            override fun compile(options: CompileOptions): ICScript {
                val compileContext = CompileContext(options)
                return object : ICScript {
                    override val context: CompileContext = compileContext
                    override val statements: List<ICScriptStatement> = entries.flatMap { it.compile(context) }
                }
            }

        }
    }
}

fun ICScriptBuilder.appendInstruction(vararg instructions: Instruction): ICScriptBuilder =
    appendEntry { instructions.toList() }