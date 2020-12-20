package com.github.scottbot95.stationeers.ic.util

expect class UUID {
    override fun toString(): String

    companion object {
        fun fromString(string: String): UUID

        fun randomUUID(): UUID
    }
}

inline val UUID.Companion.random get() = randomUUID()
