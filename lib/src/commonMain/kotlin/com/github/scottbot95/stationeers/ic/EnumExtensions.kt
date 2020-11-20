package com.github.scottbot95.stationeers.ic

inline fun <reified E : Enum<*>> E?.next(): E? = when (this) {
    null -> enumValues<E>().first()
    enumValues<E>().last() -> null
    else -> enumValues<E>()[this.ordinal + 1]
}

inline fun <reified E : Enum<*>> E?.prev(): E? = when (this) {
    null -> enumValues<E>().last()
    enumValues<E>().first() -> null
    else -> enumValues<E>()[this.ordinal - 1]
}
