package com.freeweights.app.util

import kotlin.math.abs

data class PlateLoad(
    val platesPerSide: List<Double>,
    val loadedWeight: Double,
    val remainder: Double,
    val isExact: Boolean,
)

fun calculatePlates(
    targetWeight: Double,
    barWeight: Double,
    availablePlates: List<Double>,
): PlateLoad {
    if (targetWeight <= barWeight || availablePlates.isEmpty()) {
        return PlateLoad(emptyList(), barWeight, targetWeight - barWeight, targetWeight == barWeight)
    }

    var perSide = (targetWeight - barWeight) / 2.0
    val plates = mutableListOf<Double>()

    availablePlates.filter { it > 0 }.sortedDescending().forEach { plate ->
        while (perSide + 0.0001 >= plate) {
            plates += plate
            perSide -= plate
        }
    }

    val loaded = barWeight + plates.sum() * 2.0
    val remainder = targetWeight - loaded
    return PlateLoad(
        platesPerSide = plates,
        loadedWeight = loaded,
        remainder = remainder,
        isExact = abs(remainder) < 0.001,
    )
}

fun configuredSetWeight(
    targetWeight: Double,
    barWeight: Double,
    availablePlates: List<Double>,
): Double = calculatePlates(targetWeight, barWeight, availablePlates).loadedWeight
