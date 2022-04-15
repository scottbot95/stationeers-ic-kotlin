package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel

fun icScript(init: ICScriptTopLevelScope.() -> Unit): ICScriptTopLevel {
    val scope = ICScriptTopLevelContainer(ICScriptContext())
    init(scope)
    return scope.toTopLevel()
}
