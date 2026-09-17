package com.freeweights.app.util

import com.freeweights.app.model.ActiveWorkout

fun nextWorkoutSetWeight(active: ActiveWorkout, prescribedWeight: Double): Double =
    active.manualWeight ?: prescribedWeight
