package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.ConstantReadOnlyProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class AliasedScriptValueDelegateProvider<T : Any>(
    private val container: AliasedScriptValueContainer<T>,
    private val desiredValue: T? = null,
    private val name: String? = null,
) {
    operator fun provideDelegate(
        thisRef: Any?,
        prop: KProperty<*>
    ): ReadOnlyProperty<Any?, AliasedScriptValue<T>> =
        ConstantReadOnlyProperty(container.newAliasedValue(desiredValue, name ?: prop.name))
}
