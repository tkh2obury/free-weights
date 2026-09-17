package com.freeweights.app.util

import com.freeweights.app.model.RestTimerState
import kotlin.math.ceil

fun remainingTimerSeconds(timer: RestTimerState, now: Long = System.currentTimeMillis()): Int =
    timer.endsAt?.let { ceil((it - now).coerceAtLeast(0L) / 1000.0).toInt() }
        ?: timer.pausedRemainingSeconds.coerceAtLeast(0)

fun startRestTimer(timer: RestTimerState, now: Long = System.currentTimeMillis()): RestTimerState {
    val remaining = remainingTimerSeconds(timer, now).takeIf { it > 0 } ?: timer.durationSeconds
    return timer.copy(
        pausedRemainingSeconds = remaining,
        endsAt = now + remaining * 1_000L,
    )
}

fun pauseRestTimer(timer: RestTimerState, now: Long = System.currentTimeMillis()): RestTimerState =
    timer.copy(
        pausedRemainingSeconds = remainingTimerSeconds(timer, now),
        endsAt = null,
    )

fun resetRestTimer(timer: RestTimerState): RestTimerState = timer.copy(
    pausedRemainingSeconds = timer.durationSeconds,
    endsAt = null,
)

fun completedRestTimer(timer: RestTimerState): RestTimerState = timer.copy(
    pausedRemainingSeconds = 0,
    endsAt = null,
)

fun prepareExerciseRestTimer(
    timer: RestTimerState,
    durationSeconds: Int,
    title: String,
    ownerKey: String,
): RestTimerState {
    val canReplace = timer.endsAt == null &&
        (timer.ownerKey == null || timer.pausedRemainingSeconds == timer.durationSeconds || timer.pausedRemainingSeconds == 0)
    if (!canReplace) return timer
    val duration = durationSeconds.coerceAtLeast(1)
    return RestTimerState(
        durationSeconds = duration,
        pausedRemainingSeconds = duration,
        title = title,
        ownerKey = ownerKey,
    )
}
