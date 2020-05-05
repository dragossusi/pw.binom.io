package pw.binom.process

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import platform.windows.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.thread.FreezedStack
import pw.binom.thread.Thread

private class SignalListener(val signal: Signal.Type, handler: (Signal.Type) -> Unit) : Closeable {
    private val handler = StableRef.create(handler).asCPointer()
    fun call(signal: Signal.Type) {
        val handlerPtr = handler.asStableRef<(Signal.Type) -> Unit>().get()
        handlerPtr(signal)
    }

    override fun close() {
        handler.asStableRef<(Signal.Type) -> Unit>().dispose()
    }
}

@SharedImmutable
private val signals = FreezedStack<SignalListener>()

@SharedImmutable
private val inited = AtomicBoolean(false)

actual object Signal {

    actual enum class Type(val code: Int) {
        CTRL_C(CTRL_C_EVENT),
        CTRL_B(CTRL_BREAK_EVENT),
        CLOSE(CTRL_CLOSE_EVENT),
        LOGOFF(CTRL_LOGOFF_EVENT),
        SHUTDOWN(CTRL_SHUTDOWN_EVENT)
    }

    actual fun listen(signal: Type, handler: (Signal.Type) -> Unit): Closeable {
        if (inited.compareAndSet(expected = false, new = true)) {
            println("Start listening...")
            val r = SetConsoleCtrlHandler(globalHandler, TRUE)
            println("r==>$r")
        } else {
            println("Can't start listening signals")
        }
        val listener = SignalListener(signal, handler)
        signals.pushLast(listener)
        return Closeable {
            val it = signals.iterator()
            while (it.hasNext()) {
                val e = it.next()
                if (e === listener) {
                    e.close()
                    it.remove()
                    break
                }
            }
        }
    }

    actual fun closeAll() {
        signals.forEach {
            it.close()
        }
        signals.clear()
    }
}

private val globalHandler = staticCFunction<DWORD, WINBOOL> handler@{ signal ->
    initRuntimeIfNeeded()
    try {
        println("Signal Event")
        println("signal=$signal")
        Thread.sleep(1000)
//        val signalI = signal.toInt()
//        println("signalI=$signalI")
//        return@handler FALSE
        val type = Signal.Type.values().find { it.code.toUInt() == signal }
        println("Type: $type")
        Thread.sleep(1000)
        if (type == null) {
            println("Can't determinate type. Code=${signal}")
            return@handler FALSE
        }
        val signalObj = signals.find { it.signal == type }
        if (signalObj == null) {
            println("Can't find listener for $type")
            return@handler FALSE
        }
        println("Call listener...")
        signalObj.call(type)
        return@handler TRUE
    } catch (e: Throwable) {
        println("ERROR: ${e}")
        e.printStackTrace()
        return@handler FALSE
    }

}