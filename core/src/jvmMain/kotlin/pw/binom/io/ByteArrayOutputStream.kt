package pw.binom.io

import java.io.ByteArrayOutputStream as JByteArrayOutputStream


actual class ByteArrayOutputStream actual constructor(capacity: Int, capacityFactor: Float) : OutputStream {

    private val native = JByteArrayOutputStream(capacity)

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        native.write(data, offset, length)
        return length
    }

    override fun flush() {
        native.flush()
    }

    override fun close() {
        native.close()
    }

    actual fun toByteArray(): ByteArray =
            native.toByteArray()

}