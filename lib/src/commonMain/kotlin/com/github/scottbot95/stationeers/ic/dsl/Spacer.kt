package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.Statement
import kotlin.math.max

// Unique object to avoid Spacer detecting other Noop operations as "spacers"
// Maybe just use CompiledOperation.Noop instead?
private object CompiledSpacer : CompiledOperation(ScriptValue.EMPTY, statement = Statement.Noop)

class Spacer(private val numLines: Int, private val allowAtStart: Boolean = false) : Compilable {
    override fun compile(partial: PartialCompiledScript): PartialCompiledScript {
        return if (partial.options.minify || (!allowAtStart && partial.nextLine == 0)) {
            partial
        } else {
            val reversed = partial.operations.asReversed()
            val existingSpacers = when (reversed.firstOrNull()) {
                is CompiledSpacer -> {
                    val lastSpacer = reversed.indexOfFirst { it !is CompiledSpacer }
                    if (lastSpacer < 0) {
                        partial.numLines
                    } else {
                        lastSpacer
                    }
                }
                else -> 0
            }
            val neededSpacers = max(0, numLines - existingSpacers)
            partial + List(neededSpacers) { CompiledSpacer }
        }
    }
}
