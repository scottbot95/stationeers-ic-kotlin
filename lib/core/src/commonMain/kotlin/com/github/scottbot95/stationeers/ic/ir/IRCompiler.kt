package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel

object IRCompiler {
    fun compile(topLevel: ICScriptTopLevel): IRCompilation {
        val allStatements: MutableSet<IRStatement> = mutableSetOf()

        val functions = topLevel.functions.associate {
            val numParams = it.paramTypes.size
            val entrypoint = IRStatement.Label(it.name).apply {
                allStatements += this
            }
            val context = IRCompileContext(
                regCount = numParams.toUInt(),
                allStatements = allStatements,
                next = entrypoint::next
            )
            it.code.compile(context)
            it.name to IRFunction(it.name, entrypoint, numParams)
        }

        val topLevelEntry = IRStatement.Label("start").apply {
            allStatements += this
        }
        val context = IRCompileContext(
            regCount = 0U,
            allStatements = allStatements,
            next = topLevelEntry::next
        )
        topLevel.code.compile(context)
        // slap a halt at the end of the top level to prevent overrun into the function code
        // Will be optimized away if unnecessary (eg: user already added a halt or inifite loop)
        context += IRStatement.Halt()

        return IRCompilation(functions, topLevelEntry, allStatements.toList())
    }
}
