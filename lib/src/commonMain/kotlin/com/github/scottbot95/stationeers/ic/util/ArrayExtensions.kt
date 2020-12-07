package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.CombinedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue

fun Array<out ScriptValue<*>>.toString(context: CompileContext) = CombinedScriptValue(this.toList()).toString(context)
