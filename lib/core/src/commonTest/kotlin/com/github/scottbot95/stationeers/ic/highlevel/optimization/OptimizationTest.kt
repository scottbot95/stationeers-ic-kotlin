package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.util.compareWith
import com.github.scottbot95.stationeers.ic.util.toTreeString
import io.kotest.core.spec.style.WordSpec
import mu.KotlinLogging

val logger = KotlinLogging.logger { }

abstract class OptimizationTest(
    optimizations: List<Optimization> = Optimization.all,
    body: OptimizationTest.() -> Unit = {}
) : WordSpec() {
    private lateinit var _context: ICScriptContext
    val context: ICScriptContext get() = _context

    val optimizer = Optimizer(optimizations)

    init {
        @Suppress("LeakingThis")
        beforeTest {
            _context = ICScriptContext()
        }

        body()
    }

    fun optimizeExpr(expr: Expression): Expression {
        var oldExpr = expr
        repeat(optimizer.maxAttempts) {
            val optimized = optimizer.optimizeTree(oldExpr, context)
            logger.debug { oldExpr.toTreeString().compareWith(optimized.toTreeString()) }
            if (optimized == oldExpr) return oldExpr
            oldExpr = optimized
        }

        return oldExpr
    }
}
