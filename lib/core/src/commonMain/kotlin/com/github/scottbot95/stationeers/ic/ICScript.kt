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
    val statements: List<ICScriptStatement>

    /**
     * The [CompileContext] this [ICScript] was compiled with
     */
    val context: CompileContext

//    /**
//     * Create a [ICScriptInvocation] for this [ICScript]
//     *
//     * @return A new [ICScriptInvocation]
//     */
//    fun simulate(): ICScriptInvocation

}

/**
 * Write this [ICScript] to a string
 *
 */
fun ICScript.writeToString(): String = statements.joinToString("\n") { it.render(context) }

/**
 * Write this [ICScript] to a file
 */
fun ICScript.writeToFile(file: Path, fileSystem: FileSystem) {
    val rendered = writeToString()
    fileSystem.write(file) {
        writeUtf8(rendered)
    }
}
