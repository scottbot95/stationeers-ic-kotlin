package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.highlevel.ICFunction
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel
import com.github.scottbot95.stationeers.ic.highlevel.Statement

@ICScriptDSL
interface ICScriptTopLevelScope : ICScriptBlockScope {
    operator fun ICFunction.unaryPlus()
}

internal class ICScriptTopLevelContainer(scriptContext: ICScriptContext) :
    ICScriptBlockContainer(ICScriptBlockScopeContext(scriptContext), parent = null), ICScriptTopLevelScope {
    private val functions = mutableListOf<ICFunction>()

    init {
        // Start currentScope as this toplevel
        this.context.currentScope = this
    }

    override fun ICFunction.unaryPlus() {
        functions += this
    }

    fun toTopLevel(): ICScriptTopLevel = ICScriptTopLevel(functions, Statement.CompoundStatement(statements).expression)
}
