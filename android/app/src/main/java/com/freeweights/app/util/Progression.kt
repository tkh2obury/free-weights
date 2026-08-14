package com.freeweights.app.util

import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.WorkoutLog

fun suggestedWeight(exercise: ExercisePlan, logs: List<WorkoutLog>): Double {
    val latest = logs.filter { it.exerciseId == exercise.trackingId }.maxByOrNull { it.completedAt }
        ?: return exercise.workingWeight
    val requiredSets = exercise.workSets.size.takeIf { it > 0 } ?: exercise.targetSets
    val requiredReps = exercise.workSets.maxOfOrNull { it.reps } ?: exercise.targetReps
    val completedTarget = latest.sets >= requiredSets &&
        latest.reps >= requiredReps &&
        latest.failedSets == 0
    return if (completedTarget) latest.weight + exercise.increment else latest.weight
}

fun personalBest(logs: List<WorkoutLog>): WorkoutLog? = logs.maxByOrNull { it.weight }

fun totalVolume(logs: List<WorkoutLog>): Double = logs.sumOf { it.volume }
