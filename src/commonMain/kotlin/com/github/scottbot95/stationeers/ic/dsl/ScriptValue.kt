package com.github.scottbot95.stationeers.ic.dsl

interface ScriptValue {
    fun toString(options: CompileOptions): String
}

class StringScriptValue(val value: String): ScriptValue {
    override fun toString(options: CompileOptions): String = value
}

class NumberScriptValue(val value: Number): ScriptValue {
    override fun toString(options: CompileOptions): String = value.toString()
}