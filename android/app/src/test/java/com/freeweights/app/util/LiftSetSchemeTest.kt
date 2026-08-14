package com.freeweights.app.util

import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.WarmupSetPlan
import com.freeweights.app.model.WorkSetPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiftSetSchemeTest {
    @Test
    fun `recommended warmups rise from the bar to eighty percent`() {
        val exercise = exercise(
            warmupSets = listOf(
                WarmupSetPlan(10, 0),
                WarmupSetPlan(5, 60),
                WarmupSetPlan(3, 80),
            ),
        )

        val sets = prescribedLiftSets(exercise, 225.0, 45.0, listOf(45.0, 25.0, 10.0, 5.0, 2.5))

        assertEquals(listOf(45.0, 135.0, 180.0), sets.take(3).map { it.weight })
        assertTrue(sets.take(3).all { it.isWarmup })
    }

    @Test
    fun `pyramid offsets change weight and reps for each work set`() {
        val exercise = exercise(
            workSets = listOf(
                WorkSetPlan(10, -10.0),
                WorkSetPlan(8, 0.0),
                WorkSetPlan(6, 10.0),
                WorkSetPlan(8, 0.0),
            ),
        )

        val sets = prescribedLiftSets(exercise, 135.0, 45.0, listOf(45.0, 25.0, 10.0, 5.0, 2.5))

        assertEquals(listOf(10, 8, 6, 8), sets.map { it.reps })
        assertEquals(listOf(125.0, 135.0, 145.0, 135.0), sets.map { it.weight })
        assertFalse(sets.any { it.isWarmup })
    }

    private fun exercise(
        warmupSets: List<WarmupSetPlan> = emptyList(),
        workSets: List<WorkSetPlan> = emptyList(),
    ) = ExercisePlan(
        name = "Squat",
        targetSets = 3,
        targetReps = 5,
        workingWeight = 135.0,
        increment = 5.0,
        warmupSets = warmupSets,
        workSets = workSets,
    )
}
