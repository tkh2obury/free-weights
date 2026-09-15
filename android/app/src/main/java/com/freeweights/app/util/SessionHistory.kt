package com.freeweights.app.util

import com.freeweights.app.model.ExerciseDefinition
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WorkoutLog

fun sessionKey(log: WorkoutLog): String = log.sessionId.ifBlank { log.id }

fun deleteSession(logs: List<WorkoutLog>, sessionId: String): List<WorkoutLog> =
    logs.filterNot { sessionKey(it) == sessionId }

fun addExerciseToSession(
    logs: List<WorkoutLog>,
    sessionId: String,
    exercise: ExerciseDefinition,
): List<WorkoutLog> {
    val sessionLogs = logs.filter { sessionKey(it) == sessionId }
    require(sessionLogs.isNotEmpty()) { "Session not found" }
    require(sessionLogs.none { it.exerciseId == exercise.id }) { "Exercise already exists in this session" }
    val template = sessionLogs.maxBy { it.completedAt }
    val isRunWalk = exercise.type == ExerciseType.RUN_WALK

    return logs + WorkoutLog(
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        completedAt = template.completedAt,
        sets = if (isRunWalk) exercise.intervalRounds else exercise.targetSets,
        reps = if (isRunWalk) 1 else exercise.targetReps,
        weight = if (isRunWalk) 0.0 else exercise.workingWeight,
        sessionId = sessionId,
        planId = template.planId,
        planName = template.planName,
        dayId = template.dayId,
        dayName = template.dayName,
        exerciseType = exercise.type,
        runSeconds = if (isRunWalk) exercise.runSeconds else 0,
        walkSeconds = if (isRunWalk) exercise.walkSeconds else 0,
        intervalRounds = if (isRunWalk) exercise.intervalRounds else 0,
    )
}

fun deleteSessionExercise(logs: List<WorkoutLog>, logId: String): List<WorkoutLog> =
    logs.filterNot { it.id == logId }
