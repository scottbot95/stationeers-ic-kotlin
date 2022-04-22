package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalContracts
fun requireIdent(expr: Expression) {
    contract {
        returns() implies (expr is Expression.Ident)
    }
    if (expr !is Expression.Ident) throw IllegalArgumentException("Only Idents can be assigned at this time")
}

@OptIn(ExperimentalContracts::class)
fun Expression.asIdent(): Expression.Ident {
    requireIdent(this)
    return this
}

fun <T> List<T>.padStart(minSize: Int, defaultValue: T) = if (size < minSize) {
    arrayOfNulls<Unit>(minSize - size).map { defaultValue } + this
} else this

fun <T> List<T>.padEnd(minSize: Int, defaultValue: T) = if (size < minSize) {
    this + arrayOfNulls<Unit>(minSize - size).map { defaultValue }
} else this

fun String.compareWith(other: String, header: String? = null, separator: String = "\t"): String {
    val lines1 = listOfNotNull(header) + this.lines().padEnd(other.count { it == '\n' }, "")
    val lines2 = listOfNotNull(header) + other.lines().padEnd(lines1.size, "")
    val leftWidth = lines1.maxOf { it.length }

    return lines1.mapIndexed { i, left ->
        left.padEnd(leftWidth) + separator + lines2[i]
    }.joinToString("\n")
}
