package pw.binom.thread

import kotlin.test.*

@Ignore
class ThreadTest {

    @Test
    fun noFrostTest() {
        var vv = 0
        Thread(Runnable {
            vv++
        }).start()
        Thread.sleep(1000)
        assertEquals(1, vv)
    }

    @Test
    fun currentThreadTest() {
        val mainThread = Thread.currentThread
        var done = false
        Thread(Runnable {
            assertNotEquals(Thread.currentThread.id, mainThread.id)
            done = true
        }).start()
        Thread.sleep(1000)
        assertTrue(done)
    }
}