package com.freeweights.app.util

import com.freeweights.app.model.WorkoutLog

fun sessionKey(log: WorkoutLog): String = log.sessionId.ifBlank { log.id }

fun deleteSession(logs: List<WorkoutLog>, sessionId: String): List<WorkoutLog> =
    logs.filterNot { sessionKey(it) == sessionId }
