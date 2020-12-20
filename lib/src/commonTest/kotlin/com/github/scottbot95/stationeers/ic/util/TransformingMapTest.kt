package com.github.scottbot95.stationeers.ic.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransformingMapTest {

    val singleTransformingMap by lazy {
        TransformingMap<String, String>().apply {
            registerTransformer { key, _, newValue -> key + "_" + newValue }
        }
    }

    val multipleTransformingMap by lazy {
        TransformingMap<String, String>().apply {
            registerTransformer { _, _, newValue -> newValue + "_1" }
            registerTransformer { _, _, newValue -> newValue + "_2" }
        }
    }

    val testOtherMap = mapOf(
        "testKey" to "testValue",
        "anotherKey" to "anotherValue",
    )

    @Test
    fun testPutSingleTransformer() {
        val entry = testOtherMap.entries.first()
        singleTransformingMap[entry.key] = entry.value

        assertEquals("${entry.key}_${entry.value}", singleTransformingMap[entry.key])
    }

    @Test
    fun testPutMultipleTransformer() {
        val entry = testOtherMap.entries.first()
        multipleTransformingMap[entry.key] = entry.value

        assertEquals("${entry.value}_1_2", multipleTransformingMap[entry.key])
    }

    @Test
    fun testPutAllSingleTransformer() {
        singleTransformingMap.putAll(testOtherMap)

        val expectedMap = mapOf(
            "testKey" to "testKey_testValue",
            "anotherKey" to "anotherKey_anotherValue"
        )
        assertTrue(entriesMatch(expectedMap, singleTransformingMap))
    }

    @Test
    fun testPutAllMultipleTransformer() {
        multipleTransformingMap.putAll(testOtherMap)

        val expectedMap = mapOf(
            "testKey" to "testValue_1_2",
            "anotherKey" to "anotherValue_1_2"
        )
        assertTrue(entriesMatch(expectedMap, multipleTransformingMap))
    }

    @Test
    fun testRemoveHandler() {
        val (key, value) = testOtherMap.entries.first()

        val transform: ValueTransformer<String, String> = { _, _, _ -> "ROBOTS IN DISGUISE" }

        val transformingMap = transformingMapOf<String, String>()

        transformingMap.registerTransformer(transform)
        transformingMap[key] = value
        assertEquals("ROBOTS IN DISGUISE", transformingMap[key])

        transformingMap.removeTransformer(transform)
        transformingMap[key] = value
        assertEquals(value, transformingMap[key])
    }

    private fun entriesMatch(a: Map<*, *>, b: Map<*, *>): Boolean =
        a.entries.containsAll(b.entries) && b.entries.containsAll(a.entries)
}
