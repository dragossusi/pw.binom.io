package pw.binom.io.socket

import pw.binom.doFreeze

actual class ServerSocketChannel actual constructor() : NetworkChannel {

    private val server = SocketServer()

    actual var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }

    override val socket: Socket
        get() = server.socket


    init {
        doFreeze()
    }

    override fun close() {
        socket.close()
    }

    actual fun bind(host: String, port: Int) {
        socket.bind(host, port)
    }

    actual fun accept(): SocketChannel? {
        val socket = server.accept() ?: return null
        return SocketChannel(socket)
    }
}