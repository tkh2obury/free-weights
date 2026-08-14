package com.freeweights.app.util

import com.freeweights.app.model.WorkoutLog
import org.junit.Assert.assertEquals
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

    private fun log(sessionId: String, exerciseId: String) = WorkoutLog(
        exerciseId = exerciseId,
        exerciseName = exerciseId,
        sets = 3,
        reps = 5,
        weight = 100.0,
        sessionId = sessionId,
    )
}
