package com.github.scottbot95.stationeers.ic.ir

data class IRFunction(
    val name: String,
    val entrypoint: IRStatement,
    val numParams: Int,
)

data class IRCompilation(
    val functions: Map<String, IRFunction>,
    val topLevel: IRStatement,
) : Iterable<IRStatement> {
    override operator fun iterator(): Iterator<IRStatement> = compilationIterator()

    data class Stats(
        val totalLines: Int,
        val minLines: Int,
        val numFunctions: Int,
        val complexity: Double,
    ) {
        override fun toString(): String = """
             IR Compilation Summary:
                Num lines (with labels): $totalLines
                Num liens (no labels): $minLines
                Num Functions: $numFunctions
                Complexity rating: $complexity
        """.trimIndent()
    }
}
