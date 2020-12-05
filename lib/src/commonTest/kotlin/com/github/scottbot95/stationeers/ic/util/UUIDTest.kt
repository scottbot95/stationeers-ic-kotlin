package com.github.scottbot95.stationeers.ic.util

import kotlin.test.Test
import kotlin.test.assertEquals

class UUIDTest {

    @Test
    fun canParseFromString() {
        val stringValue = "482a9941-edc6-4445-9f4b-79a65165f5c7"
        val uuid = UUID.fromString(stringValue)

        assertEquals(stringValue, uuid.toString())
    }

    @Test
    fun createsDifferentUUIDs() {
        val testSize = 10
        val testUUIDs = mutableSetOf<UUID>().apply {
            repeat(testSize) {
                add(UUID.randomUUID())
            }
        }

        assertEquals(testSize, testUUIDs.size, "Expected to generate $testSize unique IDs")
    }
}
