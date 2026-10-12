package app.victoriousmanager.fs

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class Progress(
    val filesDone: Int,
    val filesTotal: Int,
    val bytesDone: Long,
    val bytesTotal: Long,
    val current: String,
)

sealed interface Resolution {
    data object Skip : Resolution
    data object Overwrite : Resolution
    /** Keep both: destination gets a free name like "a (1).txt". */
    data object KeepBoth : Resolution
}

fun interface ConflictResolver {
    suspend fun resolve(source: FsEntry, existing: FsEntry): Resolution
}

/** Copy / move / delete on the local file system. Cancel by cancelling the collecting coroutine. */
class FileOperations(
    private val local: LocalFileProvider,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    fun copy(sources: List<FsLocation>, destDir: FsLocation, resolver: ConflictResolver): Flow<Progress> =
        transfer(sources, destDir, resolver, move = false)

    fun move(sources: List<FsLocation>, destDir: FsLocation, resolver: ConflictResolver): Flow<Progress> =
        transfer(sources, destDir, resolver, move = true)

    fun delete(targets: List<FsLocation>): Flow<Progress> = flow {
        val total = targets.size
        targets.forEachIndexed { i, t ->
            coroutineContext.ensureActive()
            emit(Progress(i, total, 0, 0, local.file(t).name))
            local.delete(t)
        }
        emit(Progress(total, total, 0, 0, ""))
    }.flowOn(io)

    private fun transfer(
        sources: List<FsLocation>,
        destDir: FsLocation,
        resolver: ConflictResolver,
        move: Boolean,
    ): Flow<Progress> = flow {
        val dest = local.file(destDir)
        if (!dest.isDirectory) throw FsException("Destination is not a folder: ${destDir.uri}")
        val srcFiles = sources.map { local.file(it) }
        srcFiles.forEach { checkNotIntoSelf(it, dest) }

        var filesTotal = 0
        var bytesTotal = 0L
        srcFiles.forEach { s -> walk(s) { f -> if (!f.isDirectory || isLink(f)) { filesTotal++; bytesTotal += f.length() } } }

        var filesDone = 0
        var bytesDone = 0L
        suspend fun tick(name: String, bytes: Long = 0, files: Int = 0) {
            filesDone += files; bytesDone += bytes
            emit(Progress(filesDone, filesTotal, bytesDone, bytesTotal, name))
        }

        suspend fun copyNode(src: File, target: File) {
            coroutineContext.ensureActive()
            if (isLink(src)) {
                Files.copy(src.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES, java.nio.file.LinkOption.NOFOLLOW_LINKS)
                tick(src.name, 0, 1)
            } else if (src.isDirectory) {
                if (!target.exists() && !target.mkdir()) throw FsException("Cannot create folder: ${target.path}")
                for (c in src.listFiles() ?: throw FsException("Cannot read: ${src.path}")) {
                    copyNode(c, File(target, c.name))
                }
            } else {
                Files.copy(src.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                target.setLastModified(src.lastModified())
                tick(src.name, src.length(), 1)
            }
        }

        for (src in srcFiles) {
            coroutineContext.ensureActive()
            var target = File(dest, src.name)
            if (target.exists() || isLink(target)) {
                val res = resolver.resolve(local.toEntry(src), local.toEntry(target))
                when (res) {
                    Resolution.Skip -> { skipCount(src) { filesDone += it; bytesDone += src.sizeTree() }; tick(src.name); continue }
                    Resolution.KeepBoth -> target = freeName(dest, src.name)
                    Resolution.Overwrite -> {
                        if (src.canonicalFile == target.canonicalFile) { tick(src.name); continue }
                        // Merge semantic is intentionally NOT used: overwrite replaces the whole target.
                        local.deleteRecursively(target)
                    }
                }
            }
            if (move) {
                val moved = try {
                    Files.move(src.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE); true
                } catch (e: IOException) { false }
                if (moved) {
                    val n = src.countLeaves(target)
                    tick(src.name, n.second, n.first)
                } else {
                    // Cross-volume: copy then delete the source, only after the copy fully succeeded.
                    copyNode(src, target)
                    local.deleteRecursively(src)
                }
            } else {
                copyNode(src, target)
            }
        }
        tick("")
    }.flowOn(io)

    private fun checkNotIntoSelf(src: File, destDir: File) {
        val s = src.canonicalFile
        val d = destDir.canonicalFile
        if (s.isDirectory && !isLink(src) && (d == s || d.path.startsWith(s.path + File.separator))) {
            throw FsException("Cannot copy or move a folder into itself: ${src.name}")
        }
    }

    private inline fun skipCount(src: File, add: (Int) -> Unit) {
        var n = 0
        walk(src) { f -> if (!f.isDirectory || isLink(f)) n++ }
        add(n)
    }

    private fun File.sizeTree(): Long { var b = 0L; walk(this) { if (!it.isDirectory) b += it.length() }; return b }

    /** After an atomic move the source is gone; count what now lives at [target]. */
    private fun File.countLeaves(target: File): Pair<Int, Long> {
        var n = 0; var b = 0L
        walk(target) { f -> if (!f.isDirectory || isLink(f)) { n++; b += f.length() } }
        return n to b
    }

    private fun freeName(dir: File, name: String): File {
        val dot = name.lastIndexOf('.')
        val base = if (dot <= 0) name else name.substring(0, dot)
        val ext = if (dot <= 0) "" else name.substring(dot)
        var i = 1
        while (true) {
            val c = File(dir, "$base ($i)$ext")
            if (!c.exists() && !isLink(c)) return c
            i++
        }
    }

    private fun isLink(f: File) = Files.isSymbolicLink(f.toPath())

    /** Pre-order walk that never follows symlinks. */
    private inline fun walk(f: File, visit: (File) -> Unit) {
        val stack = ArrayDeque<File>()
        stack.add(f)
        while (stack.isNotEmpty()) {
            val cur = stack.removeLast()
            visit(cur)
            if (!isLink(cur) && cur.isDirectory) cur.listFiles()?.let { stack.addAll(it) }
        }
    }
}
