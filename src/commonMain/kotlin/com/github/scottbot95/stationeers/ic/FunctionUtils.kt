package com.github.scottbot95.stationeers.ic

fun <T> once(body: () -> T): () -> T {
    val value by lazy(body)
    return {
        value
    }
}