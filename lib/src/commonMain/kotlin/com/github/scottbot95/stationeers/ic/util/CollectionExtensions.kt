package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
import com.github.scottbot95.stationeers.ic.dsl.plus

fun Collection<Compilable>.compileAll(
    startContext: CompileContext
): CompileResults = fold(CompileResults(startContext)) { acc, it ->
    val results = it.compile(acc.endContext)
    acc + results
}

fun Collection<Compilable>.combine() = Compilable { compileAll(it) }

fun Array<Compilable>.compileAll(startContext: CompileContext) = this.toList().compileAll(startContext)

fun Array<Compilable>.combine() = Compilable { compileAll(it) }
