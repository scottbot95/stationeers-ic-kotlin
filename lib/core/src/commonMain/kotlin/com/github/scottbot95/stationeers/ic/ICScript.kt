package com.github.scottbot95.stationeers.ic

import okio.FileSystem
import okio.Path

/**
 * A compiled Stationeers IC script
 */
interface ICScript {

    /**
     * The array of [ICScriptStatement] that compose this [ICScript]
     */
    val statements: List<ICScriptStatement>
}

/**
 * Write this [ICScript] to a string
 *
 */
fun ICScript.writeToString(): String = statements.joinToString("\n")

/**
 * Write this [ICScript] to a file
 */
fun ICScript.writeToFile(file: Path, fileSystem: FileSystem) {
    val rendered = writeToString()
    fileSystem.write(file) {
        writeUtf8(rendered)
    }
}
