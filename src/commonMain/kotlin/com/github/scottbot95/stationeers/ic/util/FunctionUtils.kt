package com.github.scottbot95.stationeers.ic.util

internal fun <T> once(body: () -> T): () -> T {
    val value by lazy(body)
    return {
        value
    }
}
