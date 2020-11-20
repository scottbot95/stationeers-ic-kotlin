package com.github.scottbot95.stationeers.ic

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val MAX_NUMBERS = 10

private class TestLockingSequence : LockingSequence<Int>() {
    override fun nextItem(currItem: Int?): Int? = when (currItem) {
        null -> 0
        MAX_NUMBERS - 1 -> null
        else -> currItem + 1
    }

    override fun prevItem(currItem: Int?): Int? = when (currItem) {
        null -> MAX_NUMBERS - 1
        0 -> null
        else -> currItem - 1
    }
}

class LockingSequenceTest {

    private lateinit var lockingSequence: LockingSequence<Int>

    @BeforeTest
    fun setup() {
        lockingSequence = TestLockingSequence()
    }

    @Test
    fun testCanLockItems() {
        val lock = lockingSequence.tryLock(0)
        assertEquals(0, lock?.value, "Expected to lock requested item")
    }

    @Test
    fun testCannotLockAnItemTwice() {
        lockingSequence.tryLock(0)
        val lock = lockingSequence.tryLock(0)
        assertNull(lock, "Expected second lock to fail")
    }

    @Test
    fun testLockNext() {
        // lock all the items, should succeed
        repeat(MAX_NUMBERS) {
            val lock = lockingSequence.lockNext()
            assertEquals(it, lock?.value)
        }

        // try to lock one more
        val lock = lockingSequence.lockNext()
        assertNull(lock)
    }

    @Test
    fun testUnlockItem() {
        val lock1 = lockingSequence.tryLock(0)!!
        lock1.unlock()
        val lock2 = lockingSequence.tryLock(0)

        assertNotNull(lock2, "Expected to lock unlocked item")
    }

    @Test
    fun testCannotUnlockMultipleTimes() {
        val lock1 = lockingSequence.tryLock(0)!!
        lock1.unlock()
        val lock2 = lockingSequence.tryLock(0)
        lock1.unlock()

        assertNotNull(lock2)
        assertEquals(false, lockingSequence.canLock(0))
    }
}
