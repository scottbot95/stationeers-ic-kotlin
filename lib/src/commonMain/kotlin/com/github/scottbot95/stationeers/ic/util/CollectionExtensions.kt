package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileOptions
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
import com.github.scottbot95.stationeers.ic.dsl.plus

fun Collection<Compilable>.compileAll(
    options: CompileOptions,
    startContext: CompileContext
): Pair<CompileResults, CompileContext> =
    fold(CompileResults() to startContext) { acc, it ->
        val results = it.compile(options, acc.second)
        acc.first + results to acc.second + results.lines.size
    }

fun Array<Compilable>.compileAll(options: CompileOptions, startContext: CompileContext) =
    this.toList().compileAll(options, startContext)
