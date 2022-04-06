package com.github.scottbot95.stationeers.ic.testUtils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.jvm.JvmInline

typealias SnapshotMap = Map<String, List<Snapshot>>

interface Jsonizable {
    fun toJsonElement(): JsonElement
}

@JvmInline
@Serializable
value class Snapshot(val value: JsonElement) : Jsonizable {
    override fun toJsonElement(): JsonElement = value
}
