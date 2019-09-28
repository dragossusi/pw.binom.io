package pw.binom

import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.UTF8
import pw.binom.io.use

actual fun ByteArray.asUTF8String(offset: Int, length: Int): String {
    val sb = StringBuilder()
    var cur = 0
    while (cur < size) {
        val size = UTF8.utf8CharSize(this[cur])
        sb.append(UTF8.utf8toUnicode(this[cur], this, cur + 1))
        cur += size + 1
    }

    return sb.toString()
}

actual fun String.asUTF8ByteArray(): ByteArray =
        ByteArrayOutputStream().use {
            val data = ByteArray(6)
            forEach { char ->
                it.write(data, 0, UTF8.unicodeToUtf8(char, data))
            }
            it.toByteArray()
        }