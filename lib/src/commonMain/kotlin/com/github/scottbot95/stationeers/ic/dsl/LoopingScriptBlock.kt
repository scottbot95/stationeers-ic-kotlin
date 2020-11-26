package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.util.Conditional

/**
 * [ScriptBlock] that will wrap contents in a loop, optionally with a condition
 * If label is specified and [CompileOptions.minify] is not set, a label will be created at the start of the loop
 * If atLeastOnce is true, the condition will be checked after each iteration ensuring that the loop will be run
 * at least once
 */
class LoopingScriptBlock(
    label: String? = null,
    conditional: Conditional? = null,
    atLeastOnce: Boolean = true,
    shouldYield: Boolean = true,
    scope: ScriptBlock? = null
) : SimpleScriptBlock(scope) {

    val loopStart: LineReference = LineReference(label)
    val loopEnd: LineReference = LineReference()

    init {
        val yieldLine = if (shouldYield) "yield" else null

        doFirst {
            +this@LoopingScriptBlock.loopStart.inject
            if (conditional !== null && !atLeastOnce) {
                branch(conditional, this@LoopingScriptBlock.loopEnd)
            }
            yieldLine?.let { +it }
        }

        doLast {
            when {
                // We could probably do another branch here and just directly to the yield to save a single step at runtime
                conditional != null && !atLeastOnce -> jump(this@LoopingScriptBlock.loopStart)
                conditional != null -> branch(conditional, this@LoopingScriptBlock.loopStart)
                else -> jump(this@LoopingScriptBlock.loopStart)
            }

            +this@LoopingScriptBlock.loopEnd.inject
        }
    }
}
