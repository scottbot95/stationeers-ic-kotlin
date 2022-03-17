package com.github.scottbot95.stationeers.ic

import okio.FileSystem
import okio.Path

data class ExportOptions(
    var minify: Boolean = false,
)

/**
 * A compiled Stationeers IC script
 */
interface ICScript {
    /**
     * Simulate the [ICScript] to up [maxSteps] steps
     *
     * @param maxSteps Max number of steps to simulate for or indefinitely if <code>null</code>
     */
    fun simulate(maxSteps: Int? = null)

    /**
     * Write this [ICScript] to a string
     *
     * @param options Options when generating a string from the compiled script
     */
    fun writeToString(options: ExportOptions = ExportOptions()): String
}

/**
 * Write this [ICScript] to a string
 *
 * @param init Block customizing the options when generating a string from the compiled script
 *
 * @see ICScript.writeToString
 */
fun ICScript.writeToString(init: ExportOptions.() -> Unit) = writeToString(ExportOptions().apply(init))

/**
 * Write this [ICScript] to a file
 */
fun ICScript.writeToFile(file: Path, fileSystem: FileSystem, options: ExportOptions = ExportOptions()) {
    val rendered = writeToString(options)
    fileSystem.write(file) {
        writeUtf8(rendered)
    }
}

fun ICScript.writeToFile(file: Path, fileSystem: FileSystem, init: ExportOptions.() -> Unit) {
    return writeToFile(file, fileSystem, ExportOptions().apply(init))
}
