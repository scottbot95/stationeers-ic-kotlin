package com.github.scottbot95.stationeers.ic.highlevel.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.allEntrypoints
import com.github.scottbot95.stationeers.ic.ir.followChain

object GarbageCollect : IROptimization {
    override fun optimize(compilation: IRCompilation): Boolean {
        var totalErased = 0
        do {
            val visited: MutableSet<IRStatement> = mutableSetOf()
            compilation.allEntrypoints.asSequence().flatMap { it.followChain() }.forEach {
                visited += it
            }

            val erased = compilation.allStatements.size - visited.size
            totalErased += erased

            compilation.allStatements.removeAll { it !in visited }
        } while (erased != 0)

        return totalErased != 0
    }
}
