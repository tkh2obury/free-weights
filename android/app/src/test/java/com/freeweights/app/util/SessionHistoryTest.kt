package com.freeweights.app.util

import com.freeweights.app.model.ExerciseDefinition
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WorkoutLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SessionHistoryTest {
    @Test
    fun deleteSessionRemovesEveryExerciseInThatSession() {
        val logs = listOf(log("one", "squat"), log("one", "bench"), log("two", "deadlift"))

        val remaining = deleteSession(logs, "one")

        assertEquals(listOf("deadlift"), remaining.map { it.exerciseId })
    }

    @Test
    fun blankLegacySessionIdsUseTheLogIdAsTheSessionKey() {
        val legacy = log("", "squat").copy(id = "legacy")

        assertEquals(emptyList<WorkoutLog>(), deleteSession(listOf(legacy), "legacy"))
    }

    @Test
    fun addExercisePreservesSessionMetadata() {
        val existing = log("one", "squat").copy(
            completedAt = 1234L,
            planId = "plan",
            planName = "Strength",
            dayId = "day",
            dayName = "Lower",
        )
        val exercise = ExerciseDefinition(
            id = "run",
            name = "Intervals",
            type = ExerciseType.RUN_WALK,
            runSeconds = 60,
            walkSeconds = 30,
            intervalRounds = 8,
        )

        val added = addExerciseToSession(listOf(existing), "one", exercise).last()

        assertEquals("one", added.sessionId)
        assertEquals(1234L, added.completedAt)
        assertEquals("plan", added.planId)
        assertEquals("day", added.dayId)
        assertEquals(8, added.sets)
        assertEquals(ExerciseType.RUN_WALK, added.exerciseType)
    }

    @Test
    fun addExerciseRejectsDuplicateExerciseInSession() {
        assertThrows(IllegalArgumentException::class.java) {
            addExerciseToSession(
                listOf(log("one", "squat")),
                "one",
                ExerciseDefinition(id = "squat", name = "Squat"),
            )
        }
    }

    @Test
    fun addExerciseKeepsLegacySessionGrouped() {
        val legacy = log("", "squat").copy(id = "legacy")

        val added = addExerciseToSession(
            listOf(legacy),
            "legacy",
            ExerciseDefinition(id = "bench", name = "Bench"),
        ).last()

        assertEquals("legacy", added.sessionId)
    }

    @Test
    fun deleteSessionExerciseRemovesOnlySelectedEntry() {
        val squat = log("one", "squat").copy(id = "squat-log")
        val bench = log("one", "bench").copy(id = "bench-log")

        val remaining = deleteSessionExercise(listOf(squat, bench), "squat-log")

        assertEquals(listOf("bench-log"), remaining.map { it.id })
    }

    private fun log(sessionId: String, exerciseId: String) = WorkoutLog(
        exerciseId = exerciseId,
        exerciseName = exerciseId,
        sets = 3,
        reps = 5,
        weight = 100.0,
        sessionId = sessionId,
    )
}
