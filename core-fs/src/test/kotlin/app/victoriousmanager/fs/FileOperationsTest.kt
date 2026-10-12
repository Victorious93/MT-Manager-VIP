package app.victoriousmanager.fs

import java.io.File
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileOperationsTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun ops(s: TestScope): FileOperations {
        val d = StandardTestDispatcher(s.testScheduler)
        return FileOperations(LocalFileProvider(d), d)
    }
    private fun loc(f: File) = FsLocation(f.path)
    private val never = ConflictResolver { _, _ -> error("no conflict expected") }
    private fun always(r: Resolution) = ConflictResolver { _, _ -> r }

    @Test fun copiesTreeAndReportsFinalProgress() = runTest {
        val src = tmp.newFolder("src"); File(src, "sub").mkdir()
        File(src, "a.txt").writeText("hello"); File(src, "sub/b.txt").writeText("world!")
        val dst = tmp.newFolder("dst")
        val progress = ops(this).copy(listOf(loc(src)), loc(dst), never).toList()
        assertEquals("world!", File(dst, "src/sub/b.txt").readText())
        assertTrue(src.exists())
        val last = progress.last()
        assertEquals(2, last.filesTotal); assertEquals(2, last.filesDone)
        assertEquals(11L, last.bytesDone)
    }

    @Test fun moveRemovesSource() = runTest {
        val f = tmp.newFile("a.txt"); f.writeText("x")
        val dst = tmp.newFolder("dst")
        ops(this).move(listOf(loc(f)), loc(dst), never).toList()
        assertFalse(f.exists()); assertEquals("x", File(dst, "a.txt").readText())
    }

    @Test fun skipLeavesExistingTargetUntouched() = runTest {
        val f = tmp.newFile("a.txt"); f.writeText("new")
        val dst = tmp.newFolder("dst"); File(dst, "a.txt").writeText("old")
        ops(this).copy(listOf(loc(f)), loc(dst), always(Resolution.Skip)).toList()
        assertEquals("old", File(dst, "a.txt").readText())
    }

    @Test fun overwriteReplaces() = runTest {
        val f = tmp.newFile("a.txt"); f.writeText("new")
        val dst = tmp.newFolder("dst"); File(dst, "a.txt").writeText("old")
        ops(this).copy(listOf(loc(f)), loc(dst), always(Resolution.Overwrite)).toList()
        assertEquals("new", File(dst, "a.txt").readText())
    }

    @Test fun keepBothPicksFreeNames() = runTest {
        val f = tmp.newFile("a.txt"); f.writeText("new")
        val dst = tmp.newFolder("dst"); File(dst, "a.txt").writeText("old"); File(dst, "a (1).txt").writeText("old1")
        ops(this).copy(listOf(loc(f)), loc(dst), always(Resolution.KeepBoth)).toList()
        assertEquals("new", File(dst, "a (2).txt").readText())
        assertEquals("old", File(dst, "a.txt").readText())
    }

    @Test fun refusesCopyingFolderIntoItself() = runTest {
        val src = tmp.newFolder("src"); val inner = File(src, "inner").also { it.mkdir() }
        assertThrows(FsException::class.java) {
            kotlinx.coroutines.runBlocking { ops(this@runTest).copy(listOf(loc(src)), loc(inner), never).toList() }
        }
        assertFalse(File(inner, "src").exists())
    }

    @Test fun overwriteOntoSelfDoesNotDeleteTheFile() = runTest {
        val f = tmp.newFile("a.txt"); f.writeText("keep")
        ops(this).copy(listOf(loc(f)), loc(tmp.root), always(Resolution.Overwrite)).toList()
        assertEquals("keep", f.readText())
    }

    @Test fun deleteRemovesTargets() = runTest {
        val d = tmp.newFolder("d"); File(d, "x").writeText("1"); val f = tmp.newFile("f")
        ops(this).delete(listOf(loc(d), loc(f))).toList()
        assertFalse(d.exists()); assertFalse(f.exists())
    }
}
