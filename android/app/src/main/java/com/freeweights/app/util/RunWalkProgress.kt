package com.freeweights.app.util

import com.freeweights.app.model.WorkoutLog

data class RunWalkDuration(
    val rounds: Int,
    val runSecondsPerInterval: Int,
    val walkSecondsPerInterval: Int,
) {
    val runSeconds: Int get() = rounds * runSecondsPerInterval
    val walkSeconds: Int get() = rounds * walkSecondsPerInterval
    val intervalSeconds: Int get() = runSecondsPerInterval + walkSecondsPerInterval
    val totalSeconds: Int get() = runSeconds + walkSeconds
}

fun runWalkDuration(log: WorkoutLog): RunWalkDuration = RunWalkDuration(
    rounds = log.intervalRounds.coerceAtLeast(log.sets).coerceAtLeast(0),
    runSecondsPerInterval = log.runSeconds.coerceAtLeast(0),
    walkSecondsPerInterval = log.walkSeconds.coerceAtLeast(0),
)
