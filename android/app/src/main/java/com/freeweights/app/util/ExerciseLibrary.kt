package com.freeweights.app.util

import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseDefinition

fun filteredExerciseLibrary(
    exercises: List<ExerciseDefinition>,
    query: String,
): List<ExerciseDefinition> {
    val normalizedQuery = query.trim()
    return exercises
        .asSequence()
        .filter { normalizedQuery.isEmpty() || it.name.contains(normalizedQuery, ignoreCase = true) }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        .toList()
}

fun renameLibraryExercise(state: AppState, exerciseId: String, newName: String): AppState {
    val normalizedName = newName.trim()
    require(normalizedName.isNotEmpty()) { "Exercise name cannot be blank" }
    if (state.exerciseLibrary.none { it.id == exerciseId }) return state

    return state.copy(
        exerciseLibrary = state.exerciseLibrary.map { exercise ->
            if (exercise.id == exerciseId) exercise.copy(name = normalizedName) else exercise
        },
        plans = state.plans.map { plan ->
            plan.copy(
                days = plan.days.map { day ->
                    day.copy(
                        exercises = day.exercises.map { exercise ->
                            if (exercise.trackingId == exerciseId) exercise.copy(name = normalizedName) else exercise
                        },
                    )
                },
            )
        },
        logs = state.logs.map { log ->
            if (log.exerciseId == exerciseId) log.copy(exerciseName = normalizedName) else log
        },
    )
}

fun deleteLibraryExercise(state: AppState, exerciseId: String): AppState {
    val active = state.activeWorkout
    val activeUsesExercise = active?.let { workout ->
        state.plans
            .firstOrNull { it.id == workout.planId }
            ?.days
            ?.firstOrNull { it.id == workout.dayId }
            ?.exercises
            ?.any { it.trackingId == exerciseId }
    } == true

    return state.copy(
        exerciseLibrary = state.exerciseLibrary.filterNot { it.id == exerciseId },
        plans = state.plans.map { plan ->
            plan.copy(
                days = plan.days.map { day ->
                    day.copy(exercises = day.exercises.filterNot { it.trackingId == exerciseId })
                },
            )
        },
        activeWorkout = if (activeUsesExercise) null else active,
    )
}
