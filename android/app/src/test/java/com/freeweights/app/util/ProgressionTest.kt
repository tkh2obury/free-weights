package com.freeweights.app.util

import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkSetPlan
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionTest {
    private val bench = ExercisePlan("bench", "Bench press", 3, 5, 135.0, 5.0)

    @Test
    fun `completed prescription adds the increment`() {
        val logs = listOf(WorkoutLog("log", "bench", "Bench press", 1000, 3, 5, 135.0))
        assertEquals(140.0, suggestedWeight(bench, logs), 0.001)
    }

    @Test
    fun `missed prescription repeats the last weight`() {
        val logs = listOf(WorkoutLog("log", "bench", "Bench press", 1000, 3, 4, 135.0))
        assertEquals(135.0, suggestedWeight(bench, logs), 0.001)
    }

    @Test
    fun `failed set repeats the last weight`() {
        val logs = listOf(WorkoutLog("log", "bench", "Bench press", 1000, 3, 5, 135.0, failedSets = 1))
        assertEquals(135.0, suggestedWeight(bench, logs), 0.001)
    }

    @Test
    fun `shared tracking id continues progression across splits`() {
        val splitBench = ExercisePlan("instance-two", "Bench press", 3, 5, 135.0, 5.0, trackingId = "bench")
        val logs = listOf(WorkoutLog("log", "bench", "Bench press", 1000, 3, 5, 135.0))
        assertEquals(140.0, suggestedWeight(splitBench, logs), 0.001)
    }

    @Test
    fun `completed pyramid advances from its top working weight`() {
        val pyramid = bench.copy(workSets = listOf(WorkSetPlan(10, -10.0), WorkSetPlan(8, 0.0), WorkSetPlan(6, 10.0)))
        val logs = listOf(WorkoutLog("log", "bench", "Bench press", 1000, 3, 10, 145.0))

        assertEquals(150.0, suggestedWeight(pyramid, logs), 0.001)
    }
}
