package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Instruction

interface ICScriptBuilder {

    fun appendEntry(entry: Compilable): ICScriptBuilder

    fun compile(options: CompileOptions): ICScript

    companion object {
        fun standard(): ICScriptBuilder = object : ICScriptBuilder {
            private val entries: MutableList<Compilable> = mutableListOf()

            override fun appendEntry(entry: Compilable): ICScriptBuilder {
                entries.add(entry)
                return this
            }

            override fun compile(options: CompileOptions): ICScript {
                val compiledBuilder = StandardCompiledICScriptBuilder(options)
                entries.forEach {
                    it.compile(compiledBuilder)
                }
                return compiledBuilder.build()
            }
        }
    }
}

fun ICScriptBuilder.appendInstruction(vararg instructions: Instruction): ICScriptBuilder =
    appendEntry { builder -> instructions.forEach { it.compile(builder) } }
