package com.github.scottbot95.stationeers.ic.util

actual class UUID private constructor(private val value: String) {

    actual override fun toString(): String = value

    actual companion object {
        actual fun fromString(string: String): UUID = UUID(string)

        actual fun randomUUID(): UUID = fromString(NpmUUID.v4())
    }

    override fun equals(other: Any?): Boolean = if (other is UUID) {
        this.value == other.value
    } else {
        false
    }

    override fun hashCode(): Int = value.hashCode()
}

@JsModule("uuid")
external object NpmUUID {
    fun v4(): String
}
