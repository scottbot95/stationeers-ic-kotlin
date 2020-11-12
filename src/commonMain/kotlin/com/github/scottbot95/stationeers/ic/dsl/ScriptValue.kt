package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.util.ConstantReadOnlyProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ScriptValue {
    fun toString(options: CompileOptions): String
}

data class StringScriptValue(val value: String) : ScriptValue {
    override fun toString(options: CompileOptions): String = value
}

data class NumberScriptValue(val value: Number) : ScriptValue {
    override fun toString(options: CompileOptions): String = value.toString()
}

data class AliasedScriptValue<T : ScriptValue>(val alias: String?, val value: T) : ScriptValue {
    override fun toString(options: CompileOptions): String =
        if (options.minify || alias == null) value.toString(options) else alias
}

class AliasedScriptValueDelegateProvider<T : ScriptValue>(
    private val name: String? = null,
    private val createValue: (name: String) -> AliasedScriptValue<T>
) {
    operator fun provideDelegate(
        thisRef: Any?,
        prop: KProperty<*>
    ): ReadOnlyProperty<Any?, AliasedScriptValue<T>> = ConstantReadOnlyProperty(createValue(name ?: prop.name))
}
