package com.freeweights.app.util

import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WorkoutLog
import org.junit.Assert.assertEquals
import org.junit.Test

class RunWalkProgressTest {
    @Test
    fun durationBreakdownIncludesEveryRunAndWalkInterval() {
        val duration = runWalkDuration(
            WorkoutLog(
                exerciseId = "run",
                exerciseName = "Run",
                sets = 5,
                reps = 1,
                weight = 0.0,
                exerciseType = ExerciseType.RUN_WALK,
                runSeconds = 60,
                walkSeconds = 30,
                intervalRounds = 5,
            ),
        )

        assertEquals(5, duration.rounds)
        assertEquals(300, duration.runSeconds)
        assertEquals(150, duration.walkSeconds)
        assertEquals(90, duration.intervalSeconds)
        assertEquals(450, duration.totalSeconds)
    }
}
