package com.github.scottbot95.stationeers.ic.testUtils

import okio.FileSystem

internal expect object Config {
    val snapshotDir: String
    val updateSnapshots: Boolean
    val filesystem: FileSystem
}