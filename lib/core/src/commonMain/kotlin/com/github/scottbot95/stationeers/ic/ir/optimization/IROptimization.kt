package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation

fun interface IROptimization {
    /**
     * Mutate the provided [compilation] into a more optimized version
     *
     * @return True if changes were made, false otherwise
     */
    fun optimize(compilation: IRCompilation): Boolean

    companion object {
        val all: List<IROptimization> = listOf(
            TrimNoOps,
            JumpThreading,
            Deduplication
        )
    }
}
