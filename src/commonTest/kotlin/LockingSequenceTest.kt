import com.github.scottbot95.stationeers.ic.LockingSequence
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.asserter

private const val MAX_NUMBERS = 10

private class TestLockingSequence : LockingSequence<Int>() {
    override fun nextItem(currItem: Int?): Int? = when (currItem) {
        null -> 0
        MAX_NUMBERS-1 -> null
        else -> currItem + 1
    }

    override fun prevItem(currItem: Int?): Int? = when (currItem) {
        null -> MAX_NUMBERS-1
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
        asserter.assertEquals("Expected to lock requested item", 0, lock?.value)
    }

    @Test
    fun testCannotLockAnItemTwice() {
        lockingSequence.tryLock(0)
        val lock = lockingSequence.tryLock(0)
        asserter.assertNull("Expected second lock to fail", lock)
    }

    @Test
    fun testLockNext() {
        // lock all the items, should succeed
        repeat(MAX_NUMBERS) {
            val lock = lockingSequence.lockNext()
            asserter.assertEquals(null, it, lock?.value)
        }

        // try to lock one more
        val lock = lockingSequence.lockNext()
        asserter.assertNull(null, lock)
    }

    @Test
    fun testUnlockItem() {
        val lock1 = lockingSequence.tryLock(0)!!
        lock1.unlock()
        val lock2 = lockingSequence.tryLock(0)

        asserter.assertNotNull("Expected to lock unlocked item", lock2)
    }

    @Test
    fun testCannotUnlockMultipleTimes() {
        val lock1 = lockingSequence.tryLock(0)!!
        lock1.unlock()
        val lock2 = lockingSequence.tryLock(0)
        lock1.unlock()

        asserter.assertNotNull(null, lock2)
        asserter.assertEquals(null, false, lockingSequence.canLock(0))

    }
}