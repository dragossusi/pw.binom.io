package pw.binom.xml.sax

import pw.binom.Stack
import pw.binom.io.*

class XmlReaderVisiter(reader: AsyncReader) {
    private val reader = ComposeAsyncReader().addLast(reader)

    private class Record(val name: String, val visiter: XmlVisiter)

    private val visiters = Stack<Record>().asLiFoQueue()

    private suspend fun accept() {
        when (val char = reader.readNoSpace()) {
            '<' -> {
                if (reader.isNextChar { it == '!' }) {
                    reader.addFirst("<!")
                    visiters.peek().visiter.cdata(readCDATA())
                } else
                    readTag()
            }
            null -> {
            }
            else -> {
                reader.addFirst(char)
                visiters.peek().visiter.value(readTextBody())
            }
        }
    }

    suspend fun accept(visiter: XmlVisiter) {
        visiters.push(Record("ROOT", visiter))

        do {
            accept()
        } while (visiters.size > 1)
    }

    private suspend fun readCDATA(): String {
        if (!reader.readText("<![CDATA[")) {
            throw ExpectedException("<![CDATA[")
        }
        val sb = StringBuilder()
        while (true) {
            if (reader.readText("]]>"))
                break
            val c = reader.read()
            sb.append(c)
        }
        return sb.toString()
    }

    private suspend fun readTextBody(): String {
        val sb = StringBuilder()
        while (true) {
            if (reader.isNextChar { it == '<' })
                break
            sb.append(reader.read())
        }
        return sb.toString()
    }

    private suspend fun readTag() {
        reader.skipSpaces()
        if (reader.isNextChar { it == '/' }) {
            reader.read()
            val endTagName = reader.word()
            val lastNode = visiters.peek()
            if (lastNode.name != endTagName) {
                throw ExpectedException(lastNode.name)
            }

            val bb = visiters.pop()
            bb.visiter.end()
            reader.skipSpaces()
            if (!reader.readText(">"))
                throw ExpectedException(">")
            return
        }

        val nodeName = reader.word()
        val subNode = visiters.peek().visiter.subNode(nodeName)
        subNode.start()
        visiters.push(Record(nodeName, subNode))

        while (true) {
            val vvv = visiters.peek().visiter
            reader.skipSpaces()
            if (reader.isNextChar { it == '/' }) {
                reader.read()
                reader.skipSpaces()
                if (!reader.readText(">"))
                    ExpectedException(">")
                vvv.end()
                visiters.pop()
                return
            }
            if (reader.isNextChar { it == '>' }) {
                reader.read()
                accept()
                return
            }
            val attrName = reader.word()
            reader.skipSpaces()
            val nn = reader.read()
            if (nn != '=')
                throw ExpectedException("=")
            subNode.attribute(attrName, reader.readString())
        }
    }
}

internal suspend fun ComposeAsyncReader.readString(): String {
    if (read() != '"')
        throw ExpectedException("\"")
    val sb = StringBuilder()
    while (true) {
        try {
            val c = read()
            if (c == '"')
                break
            sb.append(c)
        } catch (e: EOFException) {
        }
    }
    return sb.toString()
}

private suspend fun ComposeAsyncReader.readNoSpace(): Char? {
    while (true) {
        try {
            val c = read()
            if (c != '\r' && c != '\n' && c != ' ')
                return c
        } catch (e: EOFException) {
            return null
        }
    }
}

private val Char.isBreak: Boolean
    get() = this == '\n' || this == '\r' || this == ' ' || this == '=' || this == '<' || this == '>' || this == '/'

internal suspend fun ComposeAsyncReader.readText(text: String): Boolean {
    val sb = StringBuilder()
    while (true) {
        if (text.length == sb.length)
            return true
        val c = try {
            read()
        } catch (e: EOFException) {
            addFirst(sb.toString())
            return false
        }
        sb.append(c)
        if (text[sb.length - 1] != c) {
            addFirst(sb.toString())
            return false
        }
    }
}

private suspend fun ComposeAsyncReader.isNextChar(charFunc: (Char) -> Boolean): Boolean {
    return try {
        val c = read() ?: return false
        addFirst(c)
        charFunc(c)
    } catch (e: EOFException) {
        false
    }

}

internal suspend fun ComposeAsyncReader.skipSpaces() {
    while (true) {
        try {
            val c = read()
            if (c != ' ' && c != '\n' && c != '\r') {
                addFirst(c.toString().asReader().asAsync())
                break
            }
        } catch (e: EOFException) {
            break
        }
    }
}

private fun ComposeAsyncReader.addFirst(char: Char) = addFirst(char.toString())
private fun ComposeAsyncReader.addFirst(text: String) = addFirst(text.asReader().asAsync())

internal suspend fun ComposeAsyncReader.word(): String {
    skipSpaces()
    val sb = StringBuilder()
    while (true) {
        try {
            val c = read()?:return sb.toString()
            if (c.isBreak && c != '.') {
                addFirst(c)
                return sb.toString()
            }
            sb.append(c)
        } catch (e: EOFException) {
            return sb.toString()
        }
    }
}