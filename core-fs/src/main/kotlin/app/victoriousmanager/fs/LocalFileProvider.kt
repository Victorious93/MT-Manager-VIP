package app.victoriousmanager.fs

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** [FileProvider] over java.io / NIO. All blocking work runs on [io]. */
class LocalFileProvider(private val io: CoroutineDispatcher = Dispatchers.IO) : FileProvider {

    override suspend fun list(dir: FsLocation, options: ListOptions, page: PageRequest): Page<FsEntry> =
        withContext(io) {
            val d = file(dir)
            if (!d.isDirectory) throw FsException("Not a directory: ${dir.uri}")
            val children = d.listFiles() ?: throw FsException("Cannot read directory: ${dir.uri}")
            val filter = options.nameFilter?.takeIf { it.isNotEmpty() }?.lowercase()
            val entries = ArrayList<FsEntry>(children.size)
            for (f in children) {
                coroutineContext.ensureActive()
                if (!options.showHidden && f.name.startsWith(".")) continue
                if (filter != null && !f.name.lowercase().contains(filter)) continue
                entries += toEntry(f)
            }
            entries.sortWith(comparator(options))
            val from = minOf(page.offset, entries.size)
            val to = minOf(from + page.limit, entries.size)
            Page(entries.subList(from, to).toList(), from, entries.size)
        }

    override suspend fun stat(loc: FsLocation): FsEntry = withContext(io) {
        val f = file(loc)
        if (!f.exists() && !isSymlink(f)) throw FsException("Not found: ${loc.uri}")
        toEntry(f)
    }

    override suspend fun exists(loc: FsLocation): Boolean =
        withContext(io) { file(loc).let { it.exists() || isSymlink(it) } }

    override suspend fun mkdir(parent: FsLocation, name: String): FsLocation = withContext(io) {
        val target = child(parent, name)
        if (target.exists()) throw FsException("Already exists: $name")
        if (!target.mkdir()) throw FsException("Cannot create folder: $name")
        FsLocation(target.path)
    }

    override suspend fun createFile(parent: FsLocation, name: String): FsLocation = withContext(io) {
        val target = child(parent, name)
        try {
            if (!target.createNewFile()) throw FsException("Already exists: $name")
        } catch (e: IOException) {
            throw FsException("Cannot create file: $name", e)
        }
        FsLocation(target.path)
    }

    override suspend fun rename(loc: FsLocation, newName: String): FsLocation = withContext(io) {
        val src = file(loc)
        val dst = File(src.parentFile ?: throw FsException("Cannot rename root"), validName(newName))
        if (dst.exists()) throw FsException("Already exists: $newName")
        if (!src.renameTo(dst)) throw FsException("Cannot rename ${src.name}")
        FsLocation(dst.path)
    }

    override suspend fun delete(loc: FsLocation) = withContext(io) {
        deleteRecursively(file(loc))
    }

    override fun parentOf(loc: FsLocation): FsLocation? = file(loc).parentFile?.let { FsLocation(it.path) }

    // --- internals shared with FileOperations ---

    internal fun file(loc: FsLocation): File = File(loc.uri.removePrefix("file://"))

    internal fun child(parent: FsLocation, name: String): File = File(file(parent), validName(name))

    /** Deletes [f]; symlinks are unlinked, never followed. */
    internal suspend fun deleteRecursively(f: File) {
        coroutineContext.ensureActive()
        if (!isSymlink(f) && f.isDirectory) {
            f.listFiles()?.forEach { deleteRecursively(it) }
        }
        if (!f.delete() && (f.exists() || isSymlink(f))) throw FsException("Cannot delete: ${f.path}")
    }

    internal fun toEntry(f: File): FsEntry = FsEntry(
        location = FsLocation(f.path),
        name = f.name,
        isDirectory = f.isDirectory,
        size = if (f.isDirectory) 0L else f.length(),
        lastModified = f.lastModified(),
        isHidden = f.name.startsWith("."),
    )

    private fun isSymlink(f: File) = Files.isSymbolicLink(f.toPath())

    private fun comparator(o: ListOptions): Comparator<FsEntry> {
        val base: Comparator<FsEntry> = when (o.sortField) {
            SortField.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortField.SIZE -> compareBy<FsEntry> { it.size }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortField.MODIFIED -> compareBy<FsEntry> { it.lastModified }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortField.EXTENSION -> compareBy<FsEntry, String>(String.CASE_INSENSITIVE_ORDER) { it.name.substringAfterLast('.', "") }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        }
        val directed = if (o.ascending) base else base.reversed()
        return if (o.foldersFirst) compareByDescending<FsEntry> { it.isDirectory }.then(directed) else directed
    }

    companion object {
        /** Names may not contain path separators or be relative markers. */
        fun validName(name: String): String {
            if (name.isEmpty() || name == "." || name == ".." || name.contains('/') || name.contains('\u0000')) {
                throw FsException("Invalid name: '$name'")
            }
            return name
        }
    }
}
