package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.util.Conditional

data class LoopOptions(
    val label: String? = null,
    val conditional: Conditional? = null,
    val atLeastOnce: Boolean = true,
    val shouldYield: Boolean = true,
    val spacing: Int = 1
)

/**
 * [ScriptBlock] that will wrap contents in a loop, optionally with a condition
 * If label is specified and [CompileOptions.minify] is not set, a label will be created at the start of the loop
 * If atLeastOnce is true, the condition will be checked after each iteration ensuring that the loop will be run
 * at least once
 */
class LoopingScriptBlock(
    options: LoopOptions,
    scope: ScriptBlock? = null
) : SimpleScriptBlock(scope, options.spacing) {

    constructor(label: String? = null, scope: ScriptBlock? = null) : this(LoopOptions(label), scope)

    override val start = FixedLineReference(options.label)

    init {

        doFirst {
            +this@LoopingScriptBlock.start.inject
            if (options.conditional !== null && !options.atLeastOnce) {
                branch(options.conditional, this@LoopingScriptBlock.end)
            }
            if (options.shouldYield) {
                yield()
            }
        }

        doLast {
            when {
                // We could probably do another branch here and just directly to the yield to save a single step at runtime
                options.conditional != null && !options.atLeastOnce -> jump(this@LoopingScriptBlock.start)
                options.conditional != null -> branch(options.conditional, this@LoopingScriptBlock.start)
                else -> jump(this@LoopingScriptBlock.start)
            }
        }
    }
}
