package com.freeweights.app.util

import com.freeweights.app.model.AppState

fun deleteAllProgress(state: AppState): AppState = state.copy(logs = emptyList())

fun deleteAllPlans(state: AppState): AppState = state.copy(
    plans = emptyList(),
    selectedPlanId = null,
    activeWorkout = null,
)

fun deleteAllExercises(state: AppState): AppState = state.copy(
    exerciseLibrary = emptyList(),
    plans = state.plans.map { plan ->
        plan.copy(days = plan.days.map { day -> day.copy(exercises = emptyList()) })
    },
    activeWorkout = null,
)
