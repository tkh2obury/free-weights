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

class ExerciseLibraryTest {
    private val squat = ExerciseDefinition(id = "squat", name = "Back Squat")
    private val press = ExerciseDefinition(id = "press", name = "Overhead Press")

    @Test
    fun filtersAndSortsLargeLibrariesByName() {
        val result = filteredExerciseLibrary(
            listOf(press, ExerciseDefinition(id = "bench", name = "Bench Press"), squat),
            "press",
        )

        assertEquals(listOf("Bench Press", "Overhead Press"), result.map { it.name })
    }

    @Test
    fun renamePropagatesAcrossPlansAndProgress() {
        val state = stateWithSquat()
        val renamed = renameLibraryExercise(state, "squat", "High Bar Squat")

        assertEquals("High Bar Squat", renamed.exerciseLibrary.single().name)
        assertEquals("High Bar Squat", renamed.plans.single().days.single().exercises.single().name)
        assertEquals("High Bar Squat", renamed.logs.single().exerciseName)
    }

    @Test
    fun deleteRemovesPlanUsesKeepsHistoryAndEndsAffectedWorkout() {
        val state = stateWithSquat().copy(
            activeWorkout = ActiveWorkout(planId = "plan", dayId = "day"),
        )
        val deleted = deleteLibraryExercise(state, "squat")

        assertEquals(emptyList<ExerciseDefinition>(), deleted.exerciseLibrary)
        assertEquals(emptyList<ExercisePlan>(), deleted.plans.single().days.single().exercises)
        assertEquals(1, deleted.logs.size)
        assertNull(deleted.activeWorkout)
    }

    private fun stateWithSquat() = AppState(
        exerciseLibrary = listOf(squat),
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
                                name = "Back Squat",
                                targetSets = 3,
                                targetReps = 5,
                                workingWeight = 135.0,
                                increment = 5.0,
                            ),
                        ),
                    ),
                ),
            ),
        ),
        logs = listOf(
            WorkoutLog(
                exerciseId = "squat",
                exerciseName = "Back Squat",
                sets = 3,
                reps = 5,
                weight = 135.0,
            ),
        ),
    )
}
