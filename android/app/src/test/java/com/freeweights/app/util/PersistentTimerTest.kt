package com.freeweights.app.util

import com.freeweights.app.model.RestTimerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersistentTimerTest {
    @Test
    fun `running timer is derived from its absolute end time`() {
        val running = startRestTimer(RestTimerState(durationSeconds = 90, pausedRemainingSeconds = 90), now = 1_000L)

        assertEquals(91_000L, running.endsAt)
        assertEquals(60, remainingTimerSeconds(running, now = 31_000L))
    }

    @Test
    fun `pause preserves remaining time and clears end timestamp`() {
        val running = RestTimerState(durationSeconds = 90, pausedRemainingSeconds = 90, endsAt = 91_000L)
        val paused = pauseRestTimer(running, now = 31_000L)

        assertNull(paused.endsAt)
        assertEquals(60, paused.pausedRemainingSeconds)
    }

    @Test
    fun `completed timer remains at zero until reset or restarted`() {
        val completed = completedRestTimer(RestTimerState(endsAt = 10_000L))

        assertNull(completed.endsAt)
        assertEquals(0, completed.pausedRemainingSeconds)
        assertEquals(90, resetRestTimer(completed).pausedRemainingSeconds)
    }

    @Test
    fun `switching exercise preserves a paused timer`() {
        val paused = RestTimerState(
            durationSeconds = 90,
            pausedRemainingSeconds = 37,
            title = "REST / SQUAT",
            ownerKey = "squat",
        )

        assertEquals(paused, prepareExerciseRestTimer(paused, 120, "REST / BENCH", "bench"))
    }

    @Test
    fun `new exercise applies its configured rest time when timer is idle`() {
        val prepared = prepareExerciseRestTimer(RestTimerState(), 120, "REST / BENCH", "bench")

        assertEquals(120, prepared.durationSeconds)
        assertEquals(120, prepared.pausedRemainingSeconds)
        assertEquals("bench", prepared.ownerKey)
    }
}
