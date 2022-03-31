package com.github.scottbot95.stationeers.ic.testUtils

import okio.FileSystem
import okio.NodeJsFileSystem

internal actual object Config {
    actual val snapshotDir: String
        get() = TODO("Not yet implemented")
    actual val updateSnapshots: Boolean
        get() = TODO("Not yet implemented")
    actual val filesystem: FileSystem
        get() = NodeJsFileSystem
}