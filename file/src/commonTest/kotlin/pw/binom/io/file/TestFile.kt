package pw.binom.io.file

import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestFile {
    @Test
    fun `file Exist`() {
        val f = File("testFile")
        assertFalse(f.isExist)
        FileOutputStream(f).use { }
        assertTrue(f.isExist)
        assertTrue(f.isFile)
        assertFalse(f.isDirectory)

        f.delete()
    }

    @Test
    fun `delete file`() {
        val f = File("testFile")
        FileOutputStream(f).use { }
        f.delete()
        assertFalse(f.isFile)
    }

    @Test
    fun `directory`() {
        val f = File("dir")
        assertFalse(f.isDirectory)
        f.mkdir()
        assertTrue(f.isDirectory)
        assertTrue(f.delete())
        assertFalse(f.isDirectory)
        assertFalse(f.delete())
    }
}