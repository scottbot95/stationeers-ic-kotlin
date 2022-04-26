package com.github.scottbot95.stationeers.ic.util

interface DefaultingMap<K, V> : Map<K, V> {
    override fun get(key: K): V
}

interface DefaultingMutableMap<K, V> : DefaultingMap<K, V>, MutableMap<K, V>

class DefaultingMutableMapImpl<K, V>(
    private val delegate: MutableMap<K, V> = mutableMapOf(),
    private val defaultValue: (K) -> V
) : DefaultingMutableMap<K, V>, MutableMap<K, V> by delegate {
    override fun get(key: K): V = delegate.getOrPut(key) { defaultValue(key) }
}
