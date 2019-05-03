package pw.binom.atomic

import java.util.concurrent.atomic.AtomicLong as JAtomicLong

actual class AtomicLong actual constructor(value: Long) {
    private val atom = JAtomicLong(value)

    actual fun compareAndSet(expected: Long, new: Long): Boolean =
            atom.compareAndSet(expected, new)

    actual fun compareAndSwap(expected: Long, new: Long): Long =
            atom.compareAndExchange(expected, new)

    actual fun addAndGet(delta: Long): Long =
            atom.addAndGet(delta)

    actual fun increment() {
        atom.incrementAndGet()
    }

    actual fun decrement() {
        atom.decrementAndGet()
    }

    actual var value: Long
        get() = atom.get()
        set(value) {
            atom.set(value)
        }
}