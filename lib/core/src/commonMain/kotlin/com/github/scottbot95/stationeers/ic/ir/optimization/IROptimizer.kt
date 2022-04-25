package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.stats
import mu.KotlinLogging

class IROptimizer(
    private val optimizations: List<IROptimization> = IROptimization.all,
    private val maxAttempts: Int = 100,
) {
    private val logger = KotlinLogging.logger { }

    fun optimize(compilation: IRCompilation): IRCompilation {
        logger.debug { "Attempting to optimize compilation: ${compilation.stats()}" }
        repeat(maxAttempts) { attempt ->
            logger.debug { "Optimizing IR compilation... Attempt #${attempt + 1}" }
            if (optimizations.none { it.optimize(compilation) }) {
                logger.debug { "No optimizations found. Done!" }
                return compilation
            }
            logger.debug { "Improvements found! New stats: ${compilation.stats()}" }
        }

        logger.warn { "Max attempts($maxAttempts) exceeded while trying to optimize compilation" }
        return compilation
    }
}
