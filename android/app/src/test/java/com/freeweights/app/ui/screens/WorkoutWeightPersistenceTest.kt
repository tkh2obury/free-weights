package com.freeweights.app.util

import com.freeweights.app.model.ActiveWorkout
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutWeightPersistenceTest {
    @Test
    fun `manual weight overrides the next prescribed set`() {
        val active = ActiveWorkout(
            planId = "plan",
            dayId = "day",
            currentWeight = 135.0,
            manualWeight = 135.0,
        )

        assertEquals(135.0, nextWorkoutSetWeight(active, 145.0), 0.0)
    }

    @Test
    fun `prescribed weight is used until weight is manually changed`() {
        val active = ActiveWorkout(planId = "plan", dayId = "day")

        assertEquals(145.0, nextWorkoutSetWeight(active, 145.0), 0.0)
    }
}
