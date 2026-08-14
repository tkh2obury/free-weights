package com.freeweights.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateCalculatorTest {
    @Test
    fun `225 pounds uses two 45s per side`() {
        val result = calculatePlates(225.0, 45.0, listOf(45.0, 25.0, 10.0, 5.0, 2.5))
        assertEquals(listOf(45.0, 45.0), result.platesPerSide)
        assertEquals(225.0, result.loadedWeight, 0.001)
        assertTrue(result.isExact)
    }

    @Test
    fun `odd target reports the shortfall`() {
        val result = calculatePlates(137.0, 45.0, listOf(45.0, 25.0, 10.0, 5.0, 2.5))
        assertEquals(135.0, result.loadedWeight, 0.001)
        assertEquals(2.0, result.remainder, 0.001)
        assertFalse(result.isExact)
    }

    @Test
    fun `target equal to bar needs no plates`() {
        val result = calculatePlates(20.0, 20.0, listOf(20.0, 10.0, 5.0, 2.5, 1.25))
        assertTrue(result.platesPerSide.isEmpty())
        assertTrue(result.isExact)
    }

    @Test
    fun `configured set weight includes the saved bar and available plates`() {
        assertEquals(55.0, configuredSetWeight(45.0, 55.0, listOf(45.0, 25.0, 10.0)), 0.001)
        assertEquals(135.0, configuredSetWeight(137.0, 45.0, listOf(45.0, 25.0, 10.0, 5.0, 2.5)), 0.001)
    }

    @Test
    fun `variable set volume excludes warmups`() {
        val log = com.freeweights.app.model.WorkoutLog(
            exerciseId = "squat",
            exerciseName = "Squat",
            sets = 2,
            reps = 5,
            weight = 145.0,
            setResults = listOf(
                com.freeweights.app.model.WorkoutSetResult(exerciseTrackingId = "squat", setNumber = 1, reps = 10, weight = 45.0, succeeded = true, isWarmup = true),
                com.freeweights.app.model.WorkoutSetResult(exerciseTrackingId = "squat", setNumber = 2, reps = 5, weight = 135.0, succeeded = true),
                com.freeweights.app.model.WorkoutSetResult(exerciseTrackingId = "squat", setNumber = 3, reps = 3, weight = 145.0, succeeded = true),
            ),
        )

        assertEquals(1110.0, log.volume, 0.001)
    }
}
