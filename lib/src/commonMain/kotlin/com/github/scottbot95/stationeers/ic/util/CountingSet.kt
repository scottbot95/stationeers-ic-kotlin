package com.github.scottbot95.stationeers.ic.util

class CountingSet<T> {

    private val usageCount: MutableMap<T, Int> = mutableMapOf()

    fun addOne(value: T) {
        usageCount[value] = usageCount.getOrPut(value) { 0 } + 1
    }

    fun removeOne(value: T) {
        (usageCount[value]!! - 1).let {
            if (it < 0) throw IllegalStateException("Attempted to remove $value when none are in use")
            usageCount[value] = it
        }
    }

    operator fun get(value: T): Int = usageCount[value] ?: 0
}
