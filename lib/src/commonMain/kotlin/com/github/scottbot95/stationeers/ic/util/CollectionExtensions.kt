package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.PartialCompiledScript

fun Collection<Compilable>.compileAll(
    startScript: PartialCompiledScript,
    separator: Compilable,
): PartialCompiledScript = foldIndexed(startScript) { i, acc, it ->
    val prev = if (i != 0) {
        separator.compile(acc)
    } else {
        acc
    }
    it.compile(prev)
}

fun Array<Compilable>.combine(separator: Compilable) = Compilable { compileAll(it, separator) }

fun Collection<Compilable>.combine(separator: Compilable) = Compilable { compileAll(it, separator) }

fun Array<Compilable>.compileAll(startScript: PartialCompiledScript, separator: Compilable) =
    this.toList().compileAll(startScript, separator)

// FIXME Should be able to just use a default parameter here, but can't due to bug in Kotlin
//  I think it's related to https://youtrack.jetbrains.com/issue/KT-43300
fun Collection<Compilable>.combine() = combine(Compilable.Empty)
fun Array<Compilable>.compileAll(startScript: PartialCompiledScript) = compileAll(startScript, Compilable.Empty)
fun Array<Compilable>.combine() = Compilable { compileAll(it) }
fun Collection<Compilable>.compileAll(startScript: PartialCompiledScript) = compileAll(startScript, Compilable.Empty)
