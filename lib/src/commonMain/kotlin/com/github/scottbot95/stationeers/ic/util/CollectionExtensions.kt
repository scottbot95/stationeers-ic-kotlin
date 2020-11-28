package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
import com.github.scottbot95.stationeers.ic.dsl.plus

fun Collection<Compilable>.compileAll(
    startContext: CompileContext,
    separator: Compilable,
): CompileResults = foldIndexed(CompileResults(startContext)) { i, acc, it ->
    val prev = if (i != 0) {
        acc + separator.compile(acc.endContext)
    } else {
        acc
    }
    val results = it.compile(prev.endContext)
    prev + results
}

fun Array<Compilable>.combine(separator: Compilable) = Compilable { compileAll(it, separator) }

fun Collection<Compilable>.combine(separator: Compilable) = Compilable { compileAll(it, separator) }

fun Array<Compilable>.compileAll(startContext: CompileContext, separator: Compilable) =
    this.toList().compileAll(startContext, separator)

// FIXME Should be able to just use a default parameter here, but can't due to bug in Kotlin
//  I think it's related to https://youtrack.jetbrains.com/issue/KT-43300
fun Collection<Compilable>.combine() = combine(Compilable.Empty)
fun Array<Compilable>.compileAll(startContext: CompileContext) = compileAll(startContext, Compilable.Empty)
fun Array<Compilable>.combine() = Compilable { compileAll(it) }
fun Collection<Compilable>.compileAll(startContext: CompileContext): CompileResults =
    compileAll(startContext, Compilable.Empty)
