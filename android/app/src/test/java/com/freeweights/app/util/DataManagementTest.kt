package com.freeweights.app.util

import com.freeweights.app.model.ActiveWorkout
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseDefinition
import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.WorkoutDay
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkoutPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DataManagementTest {
    private val state = AppState(
        plans = listOf(
            WorkoutPlan(
                id = "plan",
                name = "Strength",
                days = listOf(
                    WorkoutDay(
                        id = "day",
                        name = "A",
                        exercises = listOf(
                            ExercisePlan(
                                id = "slot",
                                trackingId = "squat",
                                name = "Squat",
                                targetSets = 3,
                                targetReps = 5,
                                workingWeight = 100.0,
                                increment = 5.0,
                            ),
                        ),
                    ),
                ),
            ),
        ),
        selectedPlanId = "plan",
        exerciseLibrary = listOf(ExerciseDefinition(id = "squat", name = "Squat")),
        activeWorkout = ActiveWorkout(planId = "plan", dayId = "day"),
        logs = listOf(
            WorkoutLog(exerciseId = "squat", exerciseName = "Squat", sets = 3, reps = 5, weight = 100.0),
        ),
    )

    @Test
    fun progressCanBeDeletedWithoutChangingPlansOrExercises() {
        val updated = deleteAllProgress(state)

        assertEquals(emptyList<WorkoutLog>(), updated.logs)
        assertEquals(state.plans, updated.plans)
        assertEquals(state.exerciseLibrary, updated.exerciseLibrary)
    }

    @Test
    fun plansCanBeDeletedWithoutChangingExercisesOrProgress() {
        val updated = deleteAllPlans(state)

        assertEquals(emptyList<WorkoutPlan>(), updated.plans)
        assertNull(updated.selectedPlanId)
        assertNull(updated.activeWorkout)
        assertEquals(state.exerciseLibrary, updated.exerciseLibrary)
        assertEquals(state.logs, updated.logs)
    }

    @Test
    fun exercisesCanBeDeletedWithoutChangingPlansOrProgress() {
        val updated = deleteAllExercises(state)

        assertEquals(emptyList<ExerciseDefinition>(), updated.exerciseLibrary)
        assertEquals(emptyList<ExercisePlan>(), updated.plans.single().days.single().exercises)
        assertNull(updated.activeWorkout)
        assertEquals(state.logs, updated.logs)
    }
}
