package pw.binom.io.file

import pw.binom.io.OutputStream

@Deprecated(level = DeprecationLevel.WARNING, message = "Use File.channel(AccessType.WRITE, AccessType.CREATE)")
expect class FileOutputStream(file: File, append: Boolean = false) : OutputStream {
    override fun write(data: ByteArray, offset: Int, length: Int): Int
    override fun close()
}