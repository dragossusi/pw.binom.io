package pw.binom.thread

import pw.binom.async
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore
class FixedThreadPoolTest {

    @Test
    fun test() {
        val pool = FixedThreadPool(2)
        val currentThread = Thread.currentThread.id
        var count = 0

        repeat(10) {
            async {
                pool.executeAsync {
                    count++
                    assertTrue(Thread.currentThread.id != currentThread)
                }
            }
        }
        pool.shutdown()
//        pool.close()

        assertEquals(10, count)
    }
}