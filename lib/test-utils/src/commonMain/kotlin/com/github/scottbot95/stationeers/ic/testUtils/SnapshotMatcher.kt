package com.github.scottbot95.stationeers.ic.testUtils

import io.kotest.assertions.Actual
import io.kotest.assertions.Expected
import io.kotest.assertions.failure
import io.kotest.assertions.intellijFormatError
import io.kotest.assertions.print.print
import io.kotest.core.test.TestScope
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.maps.shouldContainExactly
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.jvm.JvmInline

typealias SnapshotMap = Map<String, List<Snapshot>>

@JvmInline
@Serializable
value class Snapshot(@Contextual val value: String)

private val json = Json { prettyPrint = true }

private class SnapshotMatcher(
    private val fileSystem: FileSystem,
    private val testSuite: String,
) {
    private val expectedSnapshots by lazy { loadSnapshots(fileSystem, testSuite) }

    private val seenSnapshots = mutableMapOf<String, MutableList<Snapshot>>()

    fun matchSnapshot(testName: String, value: String): MatcherResult {
        val testCaseSnapshots = seenSnapshots.getOrPut(testName) { mutableListOf() }

        val snapshotNumber = testCaseSnapshots.size
        val expected = expectedSnapshots[testName]?.get(snapshotNumber)

        val newSnapshot = Snapshot(value)
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
        // TODO there's probably a cleaner way to do this...
        val jsonObject = buildJsonObject {
            seenSnapshots.forEach { pair ->
                putJsonArray(pair.key) {
                    pair.value.forEach {
                        add(it.value)
                    }
                }
            }
        }
        val jsonString = json.encodeToString(jsonObject)
        println("Updating snapshot file `$path`")
        fileSystem.createDirectories(Config.snapshotDir.toPath())
        fileSystem.write(path) {
            writeUtf8(jsonString)
        }
    }
}

val TestScope.matchSnapshot get() = matchSnapshot()

fun TestScope.matchSnapshot() = Matcher<String> { value ->
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
    return json.decodeFromString(fileSystem.read(path) {
        readUtf8()
    })
}