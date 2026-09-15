package com.freeweights.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutWeightInputTest {
    @Test
    fun `restored active weight takes precedence over prescribed weight`() {
        assertEquals("100", initialWorkoutWeightText(100.0, 95.0, 95.0))
    }

    @Test
    fun `prescribed weight initializes a new active set`() {
        assertEquals("95", initialWorkoutWeightText(null, 95.0, 90.0))
    }
}
