package com.freeweights.app.model

import java.util.UUID

enum class WeightUnit(val label: String) { LB("lb"), KG("kg") }

enum class ExerciseType { STRENGTH, RUN_WALK }

data class WarmupSetPlan(
    val reps: Int,
    val weightPercent: Int,
)

data class WorkSetPlan(
    val reps: Int,
    val weightOffset: Double,
)

data class ExerciseDefinition(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetSets: Int = 3,
    val targetReps: Int = 5,
    val workingWeight: Double = 45.0,
    val increment: Double = 5.0,
    val restSeconds: Int = 90,
    val type: ExerciseType = ExerciseType.STRENGTH,
    val runSeconds: Int = 60,
    val walkSeconds: Int = 60,
    val intervalRounds: Int = 5,
    val warmupSets: List<WarmupSetPlan> = emptyList(),
    val workSets: List<WorkSetPlan> = emptyList(),
)

data class ExercisePlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetSets: Int,
    val targetReps: Int,
    val workingWeight: Double,
    val increment: Double,
    val restSeconds: Int = 90,
    val trackingId: String = id,
    val type: ExerciseType = ExerciseType.STRENGTH,
    val runSeconds: Int = 60,
    val walkSeconds: Int = 60,
    val intervalRounds: Int = 5,
    val warmupSets: List<WarmupSetPlan> = emptyList(),
    val workSets: List<WorkSetPlan> = emptyList(),
)

data class WorkoutDay(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<ExercisePlan> = emptyList(),
)

data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val days: List<WorkoutDay> = emptyList(),
)

data class WorkoutSetResult(
    val id: String = UUID.randomUUID().toString(),
    val exerciseTrackingId: String,
    val setNumber: Int,
    val reps: Int,
    val weight: Double,
    val succeeded: Boolean,
    val isWarmup: Boolean = false,
)

data class ActiveWorkout(
    val sessionId: String = UUID.randomUUID().toString(),
    val startedAt: Long = System.currentTimeMillis(),
    val planId: String,
    val dayId: String,
    val currentExerciseIndex: Int = 0,
    val setResults: List<WorkoutSetResult> = emptyList(),
    val currentWeight: Double? = null,
    val intervalPhase: String? = null,
    val intervalEndsAt: Long? = null,
    val intervalPausedSeconds: Int? = null,
)

data class WorkoutLog(
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseName: String,
    val completedAt: Long = System.currentTimeMillis(),
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val sessionId: String = id,
    val planId: String = "",
    val planName: String = "Workout",
    val dayId: String = "",
    val dayName: String = "Session",
    val failedSets: Int = 0,
    val exerciseType: ExerciseType = ExerciseType.STRENGTH,
    val runSeconds: Int = 0,
    val walkSeconds: Int = 0,
    val intervalRounds: Int = 0,
    val setResults: List<WorkoutSetResult> = emptyList(),
) {
    val successfulSets: Int get() = (sets - failedSets).coerceAtLeast(0)
    val volume: Double get() = if (setResults.isNotEmpty()) {
        setResults.filter { it.succeeded && !it.isWarmup }.sumOf { it.reps * it.weight }
    } else {
        successfulSets * reps * weight
    }
}

val defaultLbPlates = listOf(45.0, 25.0, 10.0, 5.0, 2.5)
val defaultKgPlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

data class AppState(
    val unit: WeightUnit = WeightUnit.LB,
    val plans: List<WorkoutPlan> = emptyList(),
    val selectedPlanId: String? = null,
    val exerciseLibrary: List<ExerciseDefinition> = emptyList(),
    val activeWorkout: ActiveWorkout? = null,
    val logs: List<WorkoutLog> = emptyList(),
    val availableLbPlates: List<Double> = defaultLbPlates,
    val availableKgPlates: List<Double> = defaultKgPlates,
    val lbBarWeight: Double = 45.0,
    val kgBarWeight: Double = 20.0,
    val themeTextColor: String = "#00FF66",
    val themeBackgroundColor: String = "#020704",
) {
    val selectedPlan: WorkoutPlan?
        get() = plans.firstOrNull { it.id == selectedPlanId } ?: plans.firstOrNull()

    fun availablePlates(forUnit: WeightUnit = unit): List<Double> =
        if (forUnit == WeightUnit.LB) availableLbPlates else availableKgPlates

    fun barWeight(forUnit: WeightUnit = unit): Double =
        if (forUnit == WeightUnit.LB) lbBarWeight else kgBarWeight
}
