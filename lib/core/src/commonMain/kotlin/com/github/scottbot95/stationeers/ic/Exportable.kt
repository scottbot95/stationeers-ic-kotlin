package com.github.scottbot95.stationeers.ic

data class ExportOptions(
    var minify: Boolean = false,
)

data class ExportContext(
    // FIXME ewww gross a mutable type! Can we do better?
    val symbols: MutableMap<String,Any?> = mutableMapOf()
)

interface Exportable {
    /**
     * Render this [Exportable] to a string
     *
     * @param options [ExportOptions] to control how certain elements get rendered
     * @param context An [ExportContext] object for sharing state between [Exportable]s during the [export] process
     *
     * @return The rendered string representation of this [Exportable]
     */
    fun export(options: ExportOptions, context: ExportContext): String
}
