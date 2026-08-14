package com.freeweights.app.ui.screens

import com.freeweights.app.model.ActiveWorkout
import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunWalkIntervalTest {
    private val exercise = ExercisePlan(
        name = "Run walk",
        targetSets = 1,
        targetReps = 1,
        workingWeight = 0.0,
        increment = 0.0,
        type = ExerciseType.RUN_WALK,
        runSeconds = 60,
        walkSeconds = 30,
        intervalRounds = 3,
    )

    private fun active() = ActiveWorkout(planId = "plan", dayId = "day")

    @Test
    fun `skip completes current interval and pauses at next run`() {
        val skipped = skipRunWalkInterval(active().copy(intervalPhase = "WALK"), exercise)

        assertEquals(1, skipped.setResults.size)
        assertEquals("RUN", skipped.intervalPhase)
        assertEquals(60, skipped.intervalPausedSeconds)
        assertNull(skipped.intervalEndsAt)
    }

    @Test
    fun `previous removes the last completed interval`() {
        val once = skipRunWalkInterval(active(), exercise)
        val previous = previousRunWalkInterval(once, exercise)

        assertEquals(0, previous.setResults.size)
        assertEquals("RUN", previous.intervalPhase)
        assertEquals(60, previous.intervalPausedSeconds)
    }

    @Test
    fun `run completion starts timed walk phase`() {
        val next = advanceRunWalkPhase(active().copy(intervalPhase = "RUN"), exercise, 1_000L)

        assertEquals("WALK", next.intervalPhase)
        assertEquals(31_000L, next.intervalEndsAt)
    }
}
