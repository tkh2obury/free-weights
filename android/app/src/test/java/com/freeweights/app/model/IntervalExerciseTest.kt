package com.freeweights.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class IntervalExerciseTest {
    @Test
    fun `run walk exercise keeps interval configuration`() {
        val exercise = ExercisePlan(
            name = "Track intervals",
            targetSets = 1,
            targetReps = 1,
            workingWeight = 0.0,
            increment = 0.0,
            type = ExerciseType.RUN_WALK,
            runSeconds = 90,
            walkSeconds = 45,
            intervalRounds = 8,
        )

        assertEquals(ExerciseType.RUN_WALK, exercise.type)
        assertEquals(90, exercise.runSeconds)
        assertEquals(45, exercise.walkSeconds)
        assertEquals(8, exercise.intervalRounds)
    }
}
