package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.LineReference
import com.github.scottbot95.stationeers.ic.dsl.ScriptBlock
import com.github.scottbot95.stationeers.ic.dsl.reference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LabelDelegateProvider(private val block: ScriptBlock, private val label: String? = null, private val inject: Boolean = true) {
    operator fun provideDelegate(
        thisRef: Any?,
        prop: KProperty<*>
    ): ReadOnlyProperty<Any?, LineReference> {
        val name = label ?: prop.name
        return ConstantReadOnlyProperty(block.reference(name, inject))
    }
}
