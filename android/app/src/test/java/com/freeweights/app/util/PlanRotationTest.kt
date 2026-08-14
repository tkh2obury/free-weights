package com.freeweights.app.util

import com.freeweights.app.model.WorkoutDay
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkoutPlan
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanRotationTest {
    private val plan = WorkoutPlan(
        id = "plan",
        name = "Split",
        days = listOf(
            WorkoutDay(id = "a", name = "Day A"),
            WorkoutDay(id = "b", name = "Day B"),
            WorkoutDay(id = "c", name = "Day C"),
        ),
    )

    @Test
    fun `first day is selected when plan has no history`() {
        assertEquals("a", nextWorkoutDayId(plan, emptyList()))
    }

    @Test
    fun `day after most recent completed day is selected`() {
        val logs = listOf(WorkoutLog("log", "lift", "Lift", 1000, 3, 5, 100.0, planId = "plan", dayId = "b"))
        assertEquals("c", nextWorkoutDayId(plan, logs))
    }

    @Test
    fun `rotation wraps after final day`() {
        val logs = listOf(WorkoutLog("log", "lift", "Lift", 1000, 3, 5, 100.0, planId = "plan", dayId = "c"))
        assertEquals("a", nextWorkoutDayId(plan, logs))
    }
}
