package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.util.once

data class LockedItem<T>(val value: T, val unlock: () -> Unit)

abstract class LockingSequence<T> {

    private val lockedItems = mutableSetOf<T>()

    private var firstKnownUnlocked: T? by mutableLazy { nextItem(null) }
    private var lastKnownUnlocked: T? by mutableLazy { prevItem(null) }

    val nextUnlockedItem: T?
        get() {
            if (canLock(firstKnownUnlocked)) {
                return firstKnownUnlocked
            } else {
                val sequence = generateSequence(firstKnownUnlocked, this::nextItem)
                for (item in sequence) {
                    if (canLock(item)) {
                        firstKnownUnlocked = item
                        return item
                    }
                }
                return null
            }
        }

    val lastUnlockedItem: T?
        get() {
            if (canLock(lastKnownUnlocked)) {
                return lastKnownUnlocked
            } else {
                for (item in generateSequence(lastKnownUnlocked, this::prevItem)) {
                    if (canLock(item)) {
                        lastKnownUnlocked = item
                        return item
                    }
                }
                return null
            }
        }

    fun lockNext(): LockedItem<T>? = nextUnlockedItem?.let {
        tryLock(it)
    }

    fun lockLast(): LockedItem<T>? = lastUnlockedItem?.let { tryLock(it) }

    fun tryLock(item: T): LockedItem<T>? = if (canLock(item)) {
        lockedItems.add(item)
        LockedItem(item, once<Unit> { lockedItems.remove(item) })
    } else null

    fun canLock(item: T?): Boolean = item !== null && !lockedItems.contains(item)

    protected abstract fun nextItem(currItem: T?): T?
    protected abstract fun prevItem(currItem: T?): T?
}
