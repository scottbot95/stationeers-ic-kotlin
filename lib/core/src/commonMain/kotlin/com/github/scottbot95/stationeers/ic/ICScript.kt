package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.simulation.ICScriptInvocation
import okio.FileSystem
import okio.Path

/**
 * A compiled Stationeers IC script
 */
interface ICScript {

    /**
     * The array of [ICScriptStatement] that compose this [ICScript]
     */
    val statements: Array<ICScriptStatement>

    /**
     * Create a [ICScriptInvocation] for this [ICScript]
     *
     * @return A new [ICScriptInvocation]
     */
    fun simulate(): ICScriptInvocation

    /**
     * Write this [ICScript] to a string
     *
     */
    fun writeToString(): String
}

/**
 * Write this [ICScript] to a file
 */
fun ICScript.writeToFile(file: Path, fileSystem: FileSystem) {
    val rendered = writeToString()
    fileSystem.write(file) {
        writeUtf8(rendered)
    }
}
