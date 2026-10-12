package app.victoriousmanager.fs

import org.junit.Assert.assertEquals
import org.junit.Test

class FsEntryTest {
    @Test
    fun entryKeepsLocation() {
        val e = FsEntry(FsLocation("file:///tmp/a"), "a", false, 1L, 2L)
        assertEquals("file:///tmp/a", e.location.uri)
    }
}
