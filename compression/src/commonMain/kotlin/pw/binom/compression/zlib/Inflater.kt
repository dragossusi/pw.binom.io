package pw.binom.compression.zlib

import pw.binom.io.Closeable



expect class Inflater : Closeable {
    constructor()
    constructor(wrap:Boolean)

    fun inflate(cursor: Cursor, input: ByteArray, output: ByteArray)
}