package com.github.scottbot95.stationeers.ic.util

typealias ValueTransformer<K, V> = (key: K, oldValue: V, newValue: V) -> V

fun <K, V> transformingMapOf(vararg pairs: Pair<K, V>) = TransformingMap(mutableMapOf(*pairs))

class TransformingMap<K, V>(private val map: MutableMap<K, V> = mutableMapOf()) : MutableMap<K, V> by map {
    private val _transformers = mutableListOf<ValueTransformer<K, V>>()

    val transformers: List<ValueTransformer<K, V>> by this::_transformers

    override fun put(key: K, value: V): V? {
        val newValue = applyTransforms(key, value)
        return map.put(key, newValue)
    }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from.mapValues { applyTransforms(it.key, it.value) })
    }

    fun registerTransformer(transformer: ValueTransformer<K, V>) {
        _transformers += transformer
    }

    fun removeTransformer(transformer: ValueTransformer<K, V>) {
        _transformers -= transformer
    }

    private fun applyTransforms(key: K, value: V): V =
        _transformers.fold(value) { acc, transform -> transform(key, value, acc) }
}
