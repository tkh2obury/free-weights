package com.freeweights.app.util

import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkoutPlan

fun nextWorkoutDayId(plan: WorkoutPlan?, logs: List<WorkoutLog>): String {
    if (plan == null || plan.days.isEmpty()) return ""
    val lastDayId = logs
        .filter { it.planId == plan.id && it.dayId.isNotBlank() }
        .maxByOrNull { it.completedAt }
        ?.dayId
        ?: return plan.days.first().id
    val lastIndex = plan.days.indexOfFirst { it.id == lastDayId }
    if (lastIndex < 0) return plan.days.first().id
    return plan.days[(lastIndex + 1) % plan.days.size].id
}
