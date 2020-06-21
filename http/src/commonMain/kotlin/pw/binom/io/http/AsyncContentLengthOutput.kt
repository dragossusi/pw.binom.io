package pw.binom.io.http

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.io.AsyncOutputStream
import pw.binom.io.StreamClosedException

open class AsyncContentLengthOutput(val stream: AsyncOutput, val contentLength: ULong) : AsyncOutput {

    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkClosed()
        if (wrote >= contentLength)
            throw IllegalStateException("All Content already send")
        if (wrote + length.toULong() > contentLength)
            throw IllegalStateException("Can't send more than Content Length")
        val r = stream.write(data, offset, length)
        wrote += r.toULong()
        return r
    }

    override suspend fun flush() {
        stream.flush()
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }

    private var wrote = 0uL
    private var closed = false

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}