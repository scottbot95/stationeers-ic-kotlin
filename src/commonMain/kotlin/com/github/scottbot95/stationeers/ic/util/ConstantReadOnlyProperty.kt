package com.github.scottbot95.stationeers.ic.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ConstantReadOnlyProperty<T, V>(private val value: V) : ReadOnlyProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V = value
}
