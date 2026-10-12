package app.fileforge.fs

/** A location in some file system. M0 placeholder; providers arrive in M1. */
@JvmInline
value class FsLocation(val uri: String)

data class FsEntry(
    val location: FsLocation,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
)
