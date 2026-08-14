package com.freeweights.app.data

import com.freeweights.app.model.ActiveWorkout
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseDefinition
import com.freeweights.app.model.WorkoutDay
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkoutPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PlanImportTest {
    @Test
    fun `plan merge preserves progress settings and active workout`() {
        val active = ActiveWorkout(planId = "current", dayId = "day")
        val log = WorkoutLog("log", "lift", "Lift", 1000, 3, 5, 100.0)
        val current = AppState(
            plans = listOf(WorkoutPlan("current", "Current")),
            selectedPlanId = "current",
            activeWorkout = active,
            logs = listOf(log),
            lbBarWeight = 35.0,
        )
        val imported = PlanImport(
            plans = listOf(WorkoutPlan("new", "Imported", listOf(WorkoutDay("day-new", "Day")))),
            exerciseLibrary = listOf(ExerciseDefinition("lift-new", "Imported lift")),
        )

        val merged = mergePlanImport(current, imported)

        assertEquals(listOf("current", "new"), merged.plans.map { it.id })
        assertEquals(listOf(log), merged.logs)
        assertEquals(35.0, merged.lbBarWeight, 0.001)
        assertNotNull(merged.activeWorkout)
        assertEquals("current", merged.selectedPlanId)
    }
}
