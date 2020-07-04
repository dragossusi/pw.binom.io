package pw.binom.compression.zlib

import kotlinx.cinterop.*
import platform.posix.memset
import platform.zlib.*
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual class Inflater actual constructor(wrap: Boolean) : Closeable {
    internal val native = nativeHeap.alloc<z_stream_s>()//malloc(sizeOf<z_stream_s>().convert())!!.reinterpret<z_stream_s>()

    private var closed = false

    private fun checkClosed() {
        if (closed)
            throw IllegalStateException("Stream already closed")
    }

    init {
        memset(native.ptr, 0, sizeOf<z_stream_s>().convert())

        if (inflateInit2(native.ptr, if (wrap) 15 else -15) != Z_OK)
            throw IOException("inflateInit2() error")
    }

    override fun close() {
        checkClosed()
        inflateEnd(native.ptr)
        nativeHeap.free(native)
        closed = true
    }

    actual fun end() {
        inflateEnd(native.ptr)
    }

    actual fun inflate(input: ByteBuffer, output: ByteBuffer): Int {
        native.avail_out = output.remaining.convert()
        native.next_out = (output.native + output.position)!!.reinterpret()

        native.avail_in = input.remaining.convert()
        native.next_in = (input.native + input.position)!!.reinterpret()
        val freeOutput = output.remaining
        val freeInput = input.remaining

        (input.position until input.limit).forEach {
            println("-->$it = ${input[it]}")
        }
        val r = inflate(native.ptr, Z_NO_FLUSH)
        if (r != Z_OK && r != Z_STREAM_END)
            throw IOException("inflate() returns [${zlibConsts(r)}]. avail_in: [${native.avail_in}], avail_out: [${native.avail_out}]")
        val wrote = freeOutput - native.avail_out.convert<Int>()

        input.position += freeInput - native.avail_in.convert<Int>()
        output.position += wrote
        return wrote
    }
}