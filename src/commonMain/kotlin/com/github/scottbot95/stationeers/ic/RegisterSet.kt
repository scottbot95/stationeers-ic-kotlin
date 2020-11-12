package com.github.scottbot95.stationeers.ic

class RegisterSet : LockingSequence<Register>() {
    override fun nextItem(currItem: Register?): Register? = currItem.prev()

    override fun prevItem(currItem: Register?): Register? = currItem.next()
}
