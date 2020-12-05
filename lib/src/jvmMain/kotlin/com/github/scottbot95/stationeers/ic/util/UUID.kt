package com.github.scottbot95.stationeers.ic.util

actual class UUID private constructor(private val uuid: java.util.UUID) {

    actual override fun toString(): String = uuid.toString()

    actual companion object {
        actual fun fromString(string: String): UUID = UUID(java.util.UUID.fromString(string))

        actual fun randomUUID(): UUID = UUID(java.util.UUID.randomUUID())
    }

    override fun equals(other: Any?): Boolean = if (other is UUID) {
        this.uuid == other.uuid
    } else {
        false
    }

    override fun hashCode(): Int = uuid.hashCode()
}
