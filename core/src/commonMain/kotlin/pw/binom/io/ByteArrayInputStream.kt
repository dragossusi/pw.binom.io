package pw.binom.io

expect class ByteArrayInputStream : InputStream {
    constructor(data: ByteArray, offset: Int = 0, length: Int = data.size - offset)
}