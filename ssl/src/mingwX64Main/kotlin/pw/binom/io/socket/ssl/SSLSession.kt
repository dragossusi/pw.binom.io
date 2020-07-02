package pw.binom.io.socket.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.refTo
import platform.openssl.*
import kotlinx.cinterop.*
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable

actual class SSLSession(val ctx:CPointer<SSL_CTX>, val ssl: CPointer<SSL>, val client: Boolean): Closeable {
    actual enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR
    }

    actual class Status(actual val state: State, actual val bytes: Int)

    private val rbio = BIO_new(BIO_s_mem()!!)!!
    private val wbio = BIO_new(BIO_s_mem()!!)!!

    private var inited = false

    private fun init(): State? {
        if (inited)
            return null
        if (client) {
            while (true) {
                val n = SSL_connect(ssl)
                if (n > 0) {
                    break
                }
                val err = SSL_get_error(ssl, n)
                if (err == SSL_ERROR_WANT_WRITE) {
                    return State.WANT_WRITE
                }

                if (err == SSL_ERROR_WANT_READ) {
                    return State.WANT_READ
                }
            }
        } else {
            SSL_accept(ssl)
        }

        inited = true
        return null
    }

    init {
        SSL_set_bio(ssl, rbio, wbio)
    }

    actual fun readNet(dst: ByteArray, offset: Int, length: Int): Int {
        val n = BIO_read(wbio, dst.refTo(0), dst.size)
        if (n < 0)
            return 0
        return n
    }

    actual fun readNet(dst: ByteDataBuffer, offset: Int, length: Int): Int {
        val n = BIO_read(wbio, dst.refTo(0), dst.size)
        if (n < 0)
            return 0
        return n
    }

    actual fun writeNet(dst: ByteArray, offset: Int, length: Int): Int {
        var len = length
        var off = offset
        var readed = 0

        while (len > 0) {
            val n = BIO_write(rbio, dst.refTo(off), len);
            if (n <= 0)
                TODO()
            readed += n

            off += n
            len -= n
        }
        return readed
    }

    actual fun writeNet(dst: ByteDataBuffer, offset: Int, length: Int): Int {
        var len = length
        var off = offset
        var readed = 0

        while (len > 0) {
            val n = BIO_write(rbio, dst.refTo(off), len);
            if (n <= 0)
                TODO()
            readed += n

            off += n
            len -= n
        }
        return readed
    }

    actual fun writeApp(src: ByteArray, offset: Int, length: Int): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_write(ssl, src.refTo(offset), length)
        if (n > 0) {
            return Status(
                    State.OK,
                    n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            else -> TODO("Unknown status $e")
        }
        return Status(
                state, 0
        )
    }

//    actual fun readApp(dst: ByteArray, offset: Int, length: Int): Status {
//        val r = init()
//        if (r != null)
//            return Status(
//                    r,
//                    0
//            )
//        val n = SSL_read(ssl, dst.refTo(offset), length)
//        if (n > 0) {
//            return Status(
//                    State.OK,
//                    n
//            )
//        }
//        val state = when (val e = SSL_get_error(ssl, n)) {
//            SSL_ERROR_WANT_READ -> State.WANT_READ
//            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
//            SSL_ERROR_SSL -> State.ERROR
//            else -> TODO("Unknown status $e")
//        }
//        return Status(
//                state, 0
//        )
//    }

    actual fun writeApp(src: ByteDataBuffer, offset: Int, length: Int): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_write(ssl, src.refTo(offset), length)
        if (n > 0) {
            return Status(
                    State.OK,
                    n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            else -> TODO("Unknown status $e")
        }
        return Status(
                state, 0
        )
    }

//    actual fun readApp(dst: ByteDataBuffer, offset: Int, length: Int): Status {
//        val r = init()
//        if (r != null)
//            return Status(
//                    r,
//                    0
//            )
//        val n = SSL_read(ssl, dst.refTo(offset), length)
//        if (n > 0) {
//            return Status(
//                    State.OK,
//                    n
//            )
//        }
//        val state = when (val e = SSL_get_error(ssl, n)) {
//            SSL_ERROR_WANT_READ -> State.WANT_READ
//            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
//            SSL_ERROR_SSL -> State.ERROR
//            else -> TODO("Unknown status $e")
//        }
//        return Status(
//                state, 0
//        )
//    }

    actual fun readNet(dst: ByteBuffer): Int {
        val n = BIO_read(wbio, dst.native, dst.remaining)
        if (n < 0)
            return 0
        dst.position += n
        return n
    }

    actual fun writeNet(dst: ByteBuffer): Int {
        var len = dst.remaining
        var off = dst.position
        var readed = 0

        while (len > 0) {
            val n = BIO_write(rbio, dst.native + off, len);
            if (n <= 0)
                TODO()
            readed += n

            off += n
            len -= n
        }
        dst.position += readed
        return readed
    }

    actual fun readApp(dst: ByteBuffer): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_read(ssl, dst.native + dst.position, dst.limit - dst.position)
        if (n > 0) {
            dst.position += n
            return Status(
                    State.OK,
                    n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            else -> TODO("Unknown status $e")
        }
        return Status(
                state, 0
        )
    }

    actual fun writeApp(src: ByteBuffer): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_write(ssl, src.native + src.position, src.limit - src.position)
        if (n > 0) {
            src.position += n
            return Status(
                    State.OK,
                    n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            else -> TODO("Unknown status $e")
        }
        return Status(
                state, 0
        )
    }

    override fun close() {
        SSL_CTX_free(ctx)
    }
}