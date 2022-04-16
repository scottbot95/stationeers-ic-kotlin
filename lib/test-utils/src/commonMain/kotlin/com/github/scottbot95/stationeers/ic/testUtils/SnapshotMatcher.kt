package com.github.scottbot95.stationeers.ic.testUtils

import io.kotest.assertions.Actual
import io.kotest.assertions.Expected
import io.kotest.assertions.failure
import io.kotest.assertions.intellijFormatError
import io.kotest.assertions.print.print
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestScope
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.maps.shouldContainExactly
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okio.FileSystem
import okio.Path.Companion.toPath

private val json = Json { prettyPrint = true }

private class SnapshotMatcher(
    private val fileSystem: FileSystem,
    private val testSuite: String,
) {
    private val expectedSnapshots by lazy { loadSnapshots(fileSystem, testSuite) }

    private val seenSnapshots = mutableMapOf<String, MutableList<Snapshot>>()

    fun matchSnapshot(testName: String, value: Any): MatcherResult {
        val testCaseSnapshots = seenSnapshots.getOrPut(testName) { mutableListOf() }

        val snapshotNumber = testCaseSnapshots.size
        val expected = expectedSnapshots[testName]?.getOrNull(snapshotNumber)

        val newSnapshot = Snapshot(value.toJsonElement())
        testCaseSnapshots += newSnapshot

        if (Config.updateSnapshots) {
            return MatcherResult(true, { "" }, { "" })
        }

        return MatcherResult(
            expected == newSnapshot,
            {
                val e = Expected(expected?.value.print())
                val a = Actual(value.print())
                val m =
                    "Expected string to match snapshot. Fix code or run `./gradlew updateSnapshots` to update the snapshots.\n"
                failure(e, a, m).message ?: (m + intellijFormatError(e, a))
            },
            { "string should not match snapshot" }
        )
    }

    fun verifySnapshots() {
        seenSnapshots shouldContainExactly expectedSnapshots
    }

    fun updateSnapshots() {
        val path = Config.snapshotDir.toPath() / "$testSuite.snapshot"
        val jsonObject = seenSnapshots.toJsonElement()
//        val jsonObject = JsonObject(seenSnapshots.mapValues { (_, v) ->
//            JsonArray(v.mapNotNull { it.toJsonElement() })
//        })
//        val jsonObject = buildJsonObject {
//            seenSnapshots.forEach { pair ->
//                putJsonArray(pair.key) {
//                    pair.value.forEach {
//                        add(it.value)
//                    }
//                }
//            }
//        }
        val jsonString = json.encodeToString(jsonObject)
        println("Updating snapshot file `$path`")
        fileSystem.createDirectories(Config.snapshotDir.toPath())
        fileSystem.write(path) {
            writeUtf8(jsonString)
        }
    }
}

val TestScope.matchSnapshot get() = matchSnapshot()

private val seenSpecs = mutableSetOf<Spec>()
fun TestScope.matchSnapshot() = Matcher<Any> { value ->
    if (testCase.spec !in seenSpecs) {
        testCase.spec.afterSpec {
            finalizeSnapshots(it::class.toDescriptor().id.value)
        }
        seenSpecs += testCase.spec
    }

    val ids = testCase.descriptor.ids()
    val matcher = getMatcher(ids.first().value)

    val snapshotName = ids.drop(1).joinToString(".") { "`${it.value}`" }
    matcher.matchSnapshot(snapshotName, value)
}

fun finalizeSnapshots(testSuite: String) {
    val matcher = getMatcher(testSuite)
    if (Config.updateSnapshots) {
        matcher.updateSnapshots()
    } else {
        matcher.verifySnapshots()
    }
}


private val matchers = mutableMapOf<String, SnapshotMatcher>()
private fun getMatcher(testSuite: String): SnapshotMatcher = matchers.getOrPut(testSuite) {
    SnapshotMatcher(Config.filesystem, testSuite)
}

private fun loadSnapshots(fileSystem: FileSystem, testSuite: String): SnapshotMap {
    val path = Config.snapshotDir.toPath() / "$testSuite.snapshot"
    if (!fileSystem.exists(path)) return mapOf()
    return try {
        json.decodeFromString(fileSystem.read(path) {
            readUtf8()
        })
    } catch (_: SerializationException) {
        // TODO make this print to stderr
        println("""Snapshot file "$path" failed to parse. Assuming no expected snapshots for $testSuite""")
        mapOf()
    }
}

private fun Any?.toJsonElement(): JsonElement = when (this) {
    is JsonElement -> this
    is Jsonizable -> toJsonElement()
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(entries.associate { (k, v) -> "$k" to v.toJsonElement() })
    is Collection<*> -> JsonArray(this.map { it.toJsonElement() })
    null -> JsonNull
    else -> throw IllegalArgumentException("Unable to convert ${this::class.simpleName} to a JsonElement")
}