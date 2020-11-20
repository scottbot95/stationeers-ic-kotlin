package com.github.scottbot95.stationeers.ic.util

interface FlagEnum

infix fun <T : FlagEnum> T.and(other: T): Set<T> = setOf(this, other)
inline infix fun <reified T : FlagEnum> Set<T>.and(other: T): Set<T> = setOf(other, *this.toTypedArray())
infix fun <T : FlagEnum> Set<T>.allOf(other: Set<T>) = this.containsAll(other)
