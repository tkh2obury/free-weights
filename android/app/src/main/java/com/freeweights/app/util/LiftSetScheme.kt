package com.freeweights.app.util

import com.freeweights.app.model.ExercisePlan

data class PrescribedLiftSet(
    val reps: Int,
    val weight: Double,
    val isWarmup: Boolean,
    val groupIndex: Int,
    val groupCount: Int,
)

fun prescribedLiftSets(
    exercise: ExercisePlan,
    workingWeight: Double,
    barWeight: Double,
    availablePlates: List<Double>,
): List<PrescribedLiftSet> {
    val warmups = exercise.warmupSets.mapIndexed { index, set ->
        val target = if (set.weightPercent <= 0) {
            barWeight
        } else {
            maxOf(barWeight, workingWeight * set.weightPercent / 100.0)
        }
        PrescribedLiftSet(
            reps = set.reps,
            weight = configuredSetWeight(target, barWeight, availablePlates),
            isWarmup = true,
            groupIndex = index,
            groupCount = exercise.warmupSets.size,
        )
    }
    val workScheme = exercise.workSets.ifEmpty {
        List(exercise.targetSets) { com.freeweights.app.model.WorkSetPlan(exercise.targetReps, 0.0) }
    }
    val work = workScheme.mapIndexed { index, set ->
        PrescribedLiftSet(
            reps = set.reps,
            weight = configuredSetWeight(
                targetWeight = (workingWeight + set.weightOffset).coerceAtLeast(barWeight),
                barWeight = barWeight,
                availablePlates = availablePlates,
            ),
            isWarmup = false,
            groupIndex = index,
            groupCount = workScheme.size,
        )
    }
    return warmups + work
}
