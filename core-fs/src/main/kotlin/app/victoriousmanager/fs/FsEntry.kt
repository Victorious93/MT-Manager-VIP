package app.victoriousmanager.fs

/** Opaque location in some file system. For the local provider [uri] is an absolute path. */
@JvmInline
value class FsLocation(val uri: String)

data class FsEntry(
    val location: FsLocation,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isHidden: Boolean = false,
)

enum class SortField { NAME, SIZE, MODIFIED, EXTENSION }

data class ListOptions(
    val showHidden: Boolean = false,
    val sortField: SortField = SortField.NAME,
    val ascending: Boolean = true,
    val foldersFirst: Boolean = true,
    /** Case-insensitive substring filter on the name; null/empty = no filter. */
    val nameFilter: String? = null,
)

data class PageRequest(val offset: Int = 0, val limit: Int = 500) {
    init {
        require(offset >= 0) { "offset must be >= 0" }
        require(limit > 0) { "limit must be > 0" }
    }
}

data class Page<T>(val items: List<T>, val offset: Int, val total: Int) {
    val hasMore: Boolean get() = offset + items.size < total
}

class FsException(message: String, cause: Throwable? = null) : Exception(message, cause)
