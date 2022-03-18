package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.simulation.ICScriptInvocation
import okio.FileSystem
import okio.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

data class ExportOptions(
    var minify: Boolean = false,
)

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
@ExperimentalContracts
fun ICScript.writeToString(init: ExportOptions.() -> Unit): String {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return writeToString(ExportOptions().apply(init))
}

/**
 * Write this [ICScript] to a file
 */
fun ICScript.writeToFile(file: Path, fileSystem: FileSystem, options: ExportOptions = ExportOptions()) {
    val rendered = writeToString(options)
    fileSystem.write(file) {
        writeUtf8(rendered)
    }
}

@ExperimentalContracts
fun ICScript.writeToFile(file: Path, fileSystem: FileSystem, init: ExportOptions.() -> Unit) {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return writeToFile(file, fileSystem, ExportOptions().apply(init))
}
