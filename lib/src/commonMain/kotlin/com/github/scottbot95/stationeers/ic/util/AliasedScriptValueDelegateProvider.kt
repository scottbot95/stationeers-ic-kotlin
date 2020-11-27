package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.AliasedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.SimpleAliasedScriptValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class AliasedScriptValueDelegateProvider<T : Any, U : AliasedScriptValue<T>>(
    private val container: AliasedScriptValueContainer<T>,
    private val desiredValue: T? = null,
    private val name: String? = null,
    private val create: AliasedScriptValueConstructor<T, U>
) {
    operator fun provideDelegate(
        thisRef: Any?,
        prop: KProperty<*>
    ): ReadOnlyProperty<Any?, U> =
        ConstantReadOnlyProperty(container.newAliasedValue(desiredValue, name ?: prop.name, create))
}

class DefaultAliasedScriptValueDelegateProvider<T : Any>(
    container: AliasedScriptValueContainer<T>,
    desiredValue: T? = null,
    name: String? = null,
) : AliasedScriptValueDelegateProvider<T, AliasedScriptValue<T>>(
    container,
    desiredValue,
    name,
    { alias: String?, value: ScriptValue<T>, release: () -> Unit -> SimpleAliasedScriptValue(alias, value, release) }
)
