package app.victoriousmanager.fs

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@JvmInline
value class JobId(val value: Long)

sealed interface JobStatus {
    data object Queued : JobStatus
    data class Running(val progress: Progress?) : JobStatus
    data object Done : JobStatus
    data class Failed(val message: String) : JobStatus
    data object Cancelled : JobStatus
}

data class JobState(val id: JobId, val title: String, val status: JobStatus)

/**
 * Runs file jobs strictly one at a time, in submission order.
 * Pause is not implemented yet (planned); cancel is.
 */
class JobQueue(private val scope: CoroutineScope) {
    private class Pending(val id: JobId, val title: String, val work: Flow<Progress>)

    private val channel = Channel<Pending>(Channel.UNLIMITED)
    private val _jobs = MutableStateFlow<List<JobState>>(emptyList())
    val jobs: StateFlow<List<JobState>> = _jobs

    private var nextId = 1L
    private var running: Pair<JobId, Job>? = null
    private val cancelRequested = mutableSetOf<JobId>()

    init {
        scope.launch {
            for (p in channel) {
                if (p.id in cancelRequested) { set(p.id) { JobStatus.Cancelled }; continue }
                val job = launch {
                    set(p.id) { JobStatus.Running(null) }
                    try {
                        p.work.collect { pr -> set(p.id) { JobStatus.Running(pr) } }
                        set(p.id) { JobStatus.Done }
                    } catch (e: CancellationException) {
                        set(p.id) { JobStatus.Cancelled }
                        if (!isActiveScope()) throw e
                    } catch (e: Exception) {
                        set(p.id) { JobStatus.Failed(e.message ?: e.javaClass.simpleName) }
                    }
                }
                running = p.id to job
                job.join()
                running = null
            }
        }
    }

    private fun isActiveScope() = scope.coroutineContext[Job]?.isActive != false

    @Synchronized
    fun submit(title: String, work: Flow<Progress>): JobId {
        val id = JobId(nextId++)
        _jobs.update { it + JobState(id, title, JobStatus.Queued) }
        channel.trySend(Pending(id, title, work))
        return id
    }

    @Synchronized
    fun cancel(id: JobId) {
        val r = running
        if (r != null && r.first == id) r.second.cancel() else cancelRequested += id
    }

    /** Drops finished jobs (Done/Failed/Cancelled) from the list. */
    fun clearFinished() = _jobs.update { l -> l.filter { it.status is JobStatus.Queued || it.status is JobStatus.Running } }

    private fun set(id: JobId, f: () -> JobStatus) =
        _jobs.update { l -> l.map { if (it.id == id) it.copy(status = f()) else it } }
}
