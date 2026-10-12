package app.victoriousmanager.fs

interface FileProvider {
    /** Lists [dir]. Sorting/filtering is applied before paging, so [Page.total] is the filtered count. */
    suspend fun list(dir: FsLocation, options: ListOptions = ListOptions(), page: PageRequest = PageRequest()): Page<FsEntry>
    suspend fun stat(loc: FsLocation): FsEntry
    suspend fun exists(loc: FsLocation): Boolean
    suspend fun mkdir(parent: FsLocation, name: String): FsLocation
    suspend fun createFile(parent: FsLocation, name: String): FsLocation
    suspend fun rename(loc: FsLocation, newName: String): FsLocation
    suspend fun delete(loc: FsLocation)
    fun parentOf(loc: FsLocation): FsLocation?
}
