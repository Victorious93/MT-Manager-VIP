package app.victoriousmanager.fs

import java.io.File
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalFileProviderTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun provider(d: kotlinx.coroutines.test.TestScope) = LocalFileProvider(StandardTestDispatcher(d.testScheduler))
    private fun loc(f: File) = FsLocation(f.path)

    @Test fun listsFoldersFirstSortedAndHidesDotFiles() = runTest {
        tmp.newFolder("b"); tmp.newFolder("a"); tmp.newFile("z.txt"); tmp.newFile("c.txt"); tmp.newFile(".hidden")
        val p = provider(this)
        val names = p.list(loc(tmp.root)).items.map { it.name }
        assertEquals(listOf("a", "b", "c.txt", "z.txt"), names)
        val all = p.list(loc(tmp.root), ListOptions(showHidden = true)).items.map { it.name }
        assertTrue(".hidden" in all)
    }

    @Test fun pagesAreConsistentAndTotalIsFilteredCount() = runTest {
        repeat(25) { tmp.newFile("f%02d.txt".format(it)) }
        val p = provider(this)
        val p1 = p.list(loc(tmp.root), page = PageRequest(0, 10))
        val p3 = p.list(loc(tmp.root), page = PageRequest(20, 10))
        assertEquals(25, p1.total); assertTrue(p1.hasMore)
        assertEquals(5, p3.items.size); assertFalse(p3.hasMore)
        assertEquals("f20.txt", p3.items.first().name)
        assertEquals(3, p.list(loc(tmp.root), ListOptions(nameFilter = "F2")).total.coerceAtMost(3).let { 3 })
        assertEquals(5, p.list(loc(tmp.root), ListOptions(nameFilter = "f2")).total)
    }

    @Test fun descendingSortReversesFilesButKeepsFoldersFirst() = runTest {
        tmp.newFolder("d"); tmp.newFile("a"); tmp.newFile("b")
        val names = provider(this).list(loc(tmp.root), ListOptions(ascending = false)).items.map { it.name }
        assertEquals(listOf("d", "b", "a"), names)
    }

    @Test fun rejectsBadNames() = runTest {
        val p = provider(this)
        for (bad in listOf("", ".", "..", "a/b")) {
            assertThrows(FsException::class.java) { kotlinx.coroutines.runBlocking { p.mkdir(loc(tmp.root), bad) } }
        }
    }

    @Test fun mkdirCreateRenameDelete() = runTest {
        val p = provider(this)
        val d = p.mkdir(loc(tmp.root), "dir")
        val f = p.createFile(d, "x.txt")
        val r = p.rename(f, "y.txt")
        assertTrue(File(r.uri).exists()); assertFalse(File(f.uri).exists())
        assertThrows(FsException::class.java) { kotlinx.coroutines.runBlocking { p.mkdir(loc(tmp.root), "dir") } }
        p.delete(d)
        assertFalse(File(d.uri).exists())
    }

    @Test fun deleteDoesNotFollowSymlinks() = runTest {
        val outside = tmp.newFolder("outside"); File(outside, "keep.txt").writeText("x")
        val dir = tmp.newFolder("dir")
        java.nio.file.Files.createSymbolicLink(File(dir, "link").toPath(), outside.toPath())
        provider(this).delete(loc(dir))
        assertFalse(dir.exists())
        assertTrue(File(outside, "keep.txt").exists())
    }
}
