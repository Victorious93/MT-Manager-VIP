package app.victoriousmanager.fs

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobQueueTest {
    private fun TestScope.statuses(q: JobQueue) = q.jobs.value.map { it.status }

    @Test fun runsJobsInOrderAndRecordsFailure() = runTest {
        val q = JobQueue(backgroundScope)
        val order = mutableListOf<Int>()
        q.submit("one", flow { order += 1 })
        q.submit("two", flow { order += 2; error("boom") })
        q.submit("three", flow { order += 3 })
        advanceUntilIdle()
        assertEquals(listOf(1, 2, 3), order)
        val s = statuses(q)
        assertEquals(JobStatus.Done, s[0])
        assertEquals(JobStatus.Failed("boom"), s[1])
        assertEquals(JobStatus.Done, s[2])
    }

    @Test fun cancelRunningJobAndQueuedJob() = runTest {
        val q = JobQueue(backgroundScope)
        val gate = CompletableDeferred<Unit>()
        val a = q.submit("a", flow { gate.await() })
        val b = q.submit("b", flow { error("must not run") })
        val c = q.submit("c", flow { })
        advanceUntilIdle()
        assertTrue(q.jobs.value.first { it.id == a }.status is JobStatus.Running)
        q.cancel(b); q.cancel(a)
        advanceUntilIdle()
        val m = q.jobs.value.associate { it.id to it.status }
        assertEquals(JobStatus.Cancelled, m[a]); assertEquals(JobStatus.Cancelled, m[b]); assertEquals(JobStatus.Done, m[c])
    }
}
