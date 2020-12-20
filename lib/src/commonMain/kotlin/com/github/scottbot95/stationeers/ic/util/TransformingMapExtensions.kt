package com.github.scottbot95.stationeers.ic.util

fun <K, V> Map<K, V>.toTransformingMap(): TransformingMap<K, V> = TransformingMap(this.toMutableMap())

fun <K, V> Map<K, V>.toTransformingMap(transformers: Iterable<ValueTransformer<K, V>>): TransformingMap<K, V> =
    toTransformingMap().apply { transformers.forEach(this::registerTransformer) }
