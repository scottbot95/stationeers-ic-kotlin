package com.github.scottbot95.stationeers.ic.testUtils

import okio.FileSystem

internal actual object Config {
    actual val snapshotDir: String by lazy { System.getProperty("snapshots.dir") }
    actual val updateSnapshots: Boolean by lazy { System.getProperty("snapshots.update").toBoolean() }
    actual val filesystem: FileSystem = FileSystem.SYSTEM
}
