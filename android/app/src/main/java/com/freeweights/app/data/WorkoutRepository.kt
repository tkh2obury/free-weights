package com.freeweights.app.data

import android.content.Context
import com.freeweights.app.model.ActiveWorkout
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseDefinition
import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WeightUnit
import com.freeweights.app.model.WorkoutDay
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkoutPlan
import com.freeweights.app.model.WorkoutSetResult
import com.freeweights.app.model.WarmupSetPlan
import com.freeweights.app.model.WorkSetPlan
import com.freeweights.app.model.defaultKgPlates
import com.freeweights.app.model.defaultLbPlates
import org.json.JSONArray
import org.json.JSONObject

data class PlanImport(
    val plans: List<WorkoutPlan>,
    val exerciseLibrary: List<ExerciseDefinition>,
)

class WorkoutRepository(context: Context) {
    private val preferences = context.getSharedPreferences("free_weights_v2", Context.MODE_PRIVATE)

    fun load(): AppState {
        val raw = preferences.getString(KEY_STATE, null) ?: return AppState()
        return runCatching { decode(JSONObject(raw)) }.getOrElse { AppState() }
    }

    fun save(state: AppState) {
        preferences.edit().putString(KEY_STATE, encode(state).toString()).apply()
    }

    fun exportJson(state: AppState): String = encode(state).toString(2)

    fun importJson(raw: String): AppState {
        val json = JSONObject(raw)
        require(json.has("plans") || json.has("logs")) { "Not a Free Weights backup" }
        require(json.optString("exportType", "full") != "plans") { "Choose Import Plans for a plan-only file" }
        return decode(json)
    }

    fun exportPlansJson(state: AppState): String {
        val full = encode(state)
        return JSONObject().apply {
            put("schemaVersion", 5)
            put("exportType", "plans")
            put("plans", full.getJSONArray("plans"))
            put("exerciseLibrary", full.getJSONArray("exerciseLibrary"))
        }.toString(2)
    }

    fun importPlansJson(raw: String): PlanImport {
        val json = JSONObject(raw)
        require(json.has("plans")) { "No plans found in this file" }
        val decoded = decode(json)
        return PlanImport(decoded.plans, decoded.exerciseLibrary)
    }

    private fun encode(state: AppState) = JSONObject().apply {
        put("schemaVersion", 5)
        put("exportType", "full")
        put("unit", state.unit.name)
        put("selectedPlanId", state.selectedPlanId)
        put("lbPlates", state.availableLbPlates.toJsonArray())
        put("kgPlates", state.availableKgPlates.toJsonArray())
        put("lbBarWeight", state.lbBarWeight)
        put("kgBarWeight", state.kgBarWeight)
        put("themeTextColor", state.themeTextColor)
        put("themeBackgroundColor", state.themeBackgroundColor)
        put("exerciseLibrary", JSONArray().apply {
            state.exerciseLibrary.forEach { exercise ->
                put(JSONObject().apply {
                    put("id", exercise.id)
                    put("name", exercise.name)
                    put("sets", exercise.targetSets)
                    put("reps", exercise.targetReps)
                    put("weight", exercise.workingWeight)
                    put("increment", exercise.increment)
                    put("restSeconds", exercise.restSeconds)
                    put("type", exercise.type.name)
                    put("runSeconds", exercise.runSeconds)
                    put("walkSeconds", exercise.walkSeconds)
                    put("intervalRounds", exercise.intervalRounds)
                    put("warmupSets", exercise.warmupSets.toWarmupJson())
                    put("workSets", exercise.workSets.toWorkSetJson())
                })
            }
        })
        put("plans", JSONArray().apply {
            state.plans.forEach { plan ->
                put(JSONObject().apply {
                    put("id", plan.id)
                    put("name", plan.name)
                    put("days", JSONArray().apply {
                        plan.days.forEach { day ->
                            put(JSONObject().apply {
                                put("id", day.id)
                                put("name", day.name)
                                put("exercises", JSONArray().apply {
                                    day.exercises.forEach { exercise ->
                                        put(JSONObject().apply {
                                            put("id", exercise.id)
                                            put("trackingId", exercise.trackingId)
                                            put("name", exercise.name)
                                            put("sets", exercise.targetSets)
                                            put("reps", exercise.targetReps)
                                            put("weight", exercise.workingWeight)
                                            put("increment", exercise.increment)
                                            put("restSeconds", exercise.restSeconds)
                                            put("type", exercise.type.name)
                                            put("runSeconds", exercise.runSeconds)
                                            put("walkSeconds", exercise.walkSeconds)
                                            put("intervalRounds", exercise.intervalRounds)
                                            put("warmupSets", exercise.warmupSets.toWarmupJson())
                                            put("workSets", exercise.workSets.toWorkSetJson())
                                        })
                                    }
                                })
                            })
                        }
                    })
                })
            }
        })
        put("activeWorkout", state.activeWorkout?.toJson() ?: JSONObject.NULL)
        put("logs", JSONArray().apply {
            state.logs.forEach { log ->
                put(JSONObject().apply {
                    put("id", log.id)
                    put("exerciseId", log.exerciseId)
                    put("exerciseName", log.exerciseName)
                    put("completedAt", log.completedAt)
                    put("sets", log.sets)
                    put("reps", log.reps)
                    put("weight", log.weight)
                    put("sessionId", log.sessionId)
                    put("planId", log.planId)
                    put("planName", log.planName)
                    put("dayId", log.dayId)
                    put("dayName", log.dayName)
                    put("failedSets", log.failedSets)
                    put("exerciseType", log.exerciseType.name)
                    put("runSeconds", log.runSeconds)
                    put("walkSeconds", log.walkSeconds)
                    put("intervalRounds", log.intervalRounds)
                    put("setResults", log.setResults.toSetResultJson())
                })
            }
        })
    }

    private fun decode(json: JSONObject): AppState {
        val plansJson = json.optJSONArray("plans") ?: JSONArray()
        val plans = buildList {
            repeat(plansJson.length()) { index ->
                val item = plansJson.getJSONObject(index)
                add(
                    WorkoutPlan(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        days = decodeDays(item.optJSONArray("days") ?: JSONArray()),
                    ),
                )
            }
        }

        val libraryJson = json.optJSONArray("exerciseLibrary") ?: JSONArray()
        val decodedLibrary = buildList {
            repeat(libraryJson.length()) { index ->
                val item = libraryJson.getJSONObject(index)
                add(
                    ExerciseDefinition(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        targetSets = item.optInt("sets", 3),
                        targetReps = item.optInt("reps", 5),
                        workingWeight = item.optDouble("weight", 45.0),
                        increment = item.optDouble("increment", 5.0),
                        restSeconds = item.optInt("restSeconds", 90),
                        type = item.exerciseType("type"),
                        runSeconds = item.optInt("runSeconds", 60),
                        walkSeconds = item.optInt("walkSeconds", 60),
                        intervalRounds = item.optInt("intervalRounds", 5),
                        warmupSets = item.optJSONArray("warmupSets").toWarmupSets(),
                        workSets = item.optJSONArray("workSets").toWorkSets(),
                    ),
                )
            }
        }
        val exerciseLibrary = if (decodedLibrary.isNotEmpty()) decodedLibrary else plans
            .flatMap { it.days }
            .flatMap { it.exercises }
            .distinctBy { it.trackingId }
            .map {
                ExerciseDefinition(
                    id = it.trackingId,
                    name = it.name,
                    targetSets = it.targetSets,
                    targetReps = it.targetReps,
                    workingWeight = it.workingWeight,
                    increment = it.increment,
                    restSeconds = it.restSeconds,
                    type = it.type,
                    runSeconds = it.runSeconds,
                    walkSeconds = it.walkSeconds,
                    intervalRounds = it.intervalRounds,
                    warmupSets = it.warmupSets,
                    workSets = it.workSets,
                )
            }

        val logsJson = json.optJSONArray("logs") ?: JSONArray()
        val logs = buildList {
            repeat(logsJson.length()) { index ->
                val item = logsJson.getJSONObject(index)
                val id = item.getString("id")
                add(
                    WorkoutLog(
                        id = id,
                        exerciseId = item.getString("exerciseId"),
                        exerciseName = item.getString("exerciseName"),
                        completedAt = item.getLong("completedAt"),
                        sets = item.getInt("sets"),
                        reps = item.getInt("reps"),
                        weight = item.getDouble("weight"),
                        sessionId = item.optString("sessionId", id),
                        planId = item.optString("planId"),
                        planName = item.optString("planName", "Workout"),
                        dayId = item.optString("dayId"),
                        dayName = item.optString("dayName", "Session"),
                        failedSets = item.optInt("failedSets", 0),
                        exerciseType = item.exerciseType("exerciseType"),
                        runSeconds = item.optInt("runSeconds", 0),
                        walkSeconds = item.optInt("walkSeconds", 0),
                        intervalRounds = item.optInt("intervalRounds", 0),
                        setResults = item.optJSONArray("setResults").toSetResults(),
                    ),
                )
            }
        }

        val selected = json.optString("selectedPlanId").takeIf { it.isNotBlank() && it != "null" }
        return AppState(
            unit = runCatching { WeightUnit.valueOf(json.optString("unit")) }.getOrDefault(WeightUnit.LB),
            plans = plans,
            selectedPlanId = selected?.takeIf { id -> plans.any { it.id == id } } ?: plans.firstOrNull()?.id,
            exerciseLibrary = exerciseLibrary,
            activeWorkout = json.optJSONObject("activeWorkout")?.toActiveWorkout(),
            logs = logs,
            availableLbPlates = json.optJSONArray("lbPlates").toDoubleList().ifEmpty { defaultLbPlates },
            availableKgPlates = json.optJSONArray("kgPlates").toDoubleList().ifEmpty { defaultKgPlates },
            lbBarWeight = json.optDouble("lbBarWeight", 45.0),
            kgBarWeight = json.optDouble("kgBarWeight", 20.0),
            themeTextColor = json.optString("themeTextColor", "#00FF66"),
            themeBackgroundColor = json.optString("themeBackgroundColor", "#020704"),
        )
    }

    private fun decodeDays(daysJson: JSONArray): List<WorkoutDay> = buildList {
        repeat(daysJson.length()) { index ->
            val item = daysJson.getJSONObject(index)
            val exercisesJson = item.optJSONArray("exercises") ?: JSONArray()
            val exercises = buildList {
                repeat(exercisesJson.length()) { exerciseIndex ->
                    val exercise = exercisesJson.getJSONObject(exerciseIndex)
                    val id = exercise.getString("id")
                    add(
                        ExercisePlan(
                            id = id,
                            trackingId = exercise.optString("trackingId", id),
                            name = exercise.getString("name"),
                            targetSets = exercise.getInt("sets"),
                            targetReps = exercise.getInt("reps"),
                            workingWeight = exercise.getDouble("weight"),
                            increment = exercise.getDouble("increment"),
                            restSeconds = exercise.optInt("restSeconds", 90),
                            type = exercise.exerciseType("type"),
                            runSeconds = exercise.optInt("runSeconds", 60),
                            walkSeconds = exercise.optInt("walkSeconds", 60),
                            intervalRounds = exercise.optInt("intervalRounds", 5),
                            warmupSets = exercise.optJSONArray("warmupSets").toWarmupSets(),
                            workSets = exercise.optJSONArray("workSets").toWorkSets(),
                        ),
                    )
                }
            }
            add(WorkoutDay(item.getString("id"), item.getString("name"), exercises))
        }
    }

    private fun ActiveWorkout.toJson() = JSONObject().apply {
        put("sessionId", sessionId)
        put("startedAt", startedAt)
        put("planId", planId)
        put("dayId", dayId)
        put("currentExerciseIndex", currentExerciseIndex)
        put("currentWeight", currentWeight)
        put("intervalPhase", intervalPhase)
        put("intervalEndsAt", intervalEndsAt)
        put("intervalPausedSeconds", intervalPausedSeconds)
        put("setResults", JSONArray().apply {
            setResults.forEach { result ->
                put(JSONObject().apply {
                    put("id", result.id)
                    put("exerciseTrackingId", result.exerciseTrackingId)
                    put("setNumber", result.setNumber)
                    put("reps", result.reps)
                    put("weight", result.weight)
                    put("succeeded", result.succeeded)
                    put("isWarmup", result.isWarmup)
                })
            }
        })
    }

    private fun JSONObject.toActiveWorkout(): ActiveWorkout {
        val resultsJson = optJSONArray("setResults") ?: JSONArray()
        val results = buildList {
            repeat(resultsJson.length()) { index ->
                val item = resultsJson.getJSONObject(index)
                add(
                    WorkoutSetResult(
                        id = item.getString("id"),
                        exerciseTrackingId = item.getString("exerciseTrackingId"),
                        setNumber = item.getInt("setNumber"),
                        reps = item.getInt("reps"),
                        weight = item.getDouble("weight"),
                        succeeded = item.getBoolean("succeeded"),
                        isWarmup = item.optBoolean("isWarmup", false),
                    ),
                )
            }
        }
        return ActiveWorkout(
            sessionId = getString("sessionId"),
            startedAt = getLong("startedAt"),
            planId = getString("planId"),
            dayId = getString("dayId"),
            currentExerciseIndex = optInt("currentExerciseIndex", 0),
            setResults = results,
            currentWeight = if (has("currentWeight") && !isNull("currentWeight")) getDouble("currentWeight") else null,
            intervalPhase = optString("intervalPhase").takeIf { it.isNotBlank() && it != "null" },
            intervalEndsAt = if (has("intervalEndsAt") && !isNull("intervalEndsAt")) getLong("intervalEndsAt") else null,
            intervalPausedSeconds = if (has("intervalPausedSeconds") && !isNull("intervalPausedSeconds")) getInt("intervalPausedSeconds") else null,
        )
    }

    private fun JSONObject.exerciseType(key: String): ExerciseType =
        runCatching { ExerciseType.valueOf(optString(key, ExerciseType.STRENGTH.name)) }
            .getOrDefault(ExerciseType.STRENGTH)

    private fun List<Double>.toJsonArray() = JSONArray().apply { forEach { put(it) } }

    private fun List<WarmupSetPlan>.toWarmupJson() = JSONArray().apply {
        forEach { set ->
            put(JSONObject().apply {
                put("reps", set.reps)
                put("weightPercent", set.weightPercent)
            })
        }
    }

    private fun List<WorkSetPlan>.toWorkSetJson() = JSONArray().apply {
        forEach { set ->
            put(JSONObject().apply {
                put("reps", set.reps)
                put("weightOffset", set.weightOffset)
            })
        }
    }

    private fun List<WorkoutSetResult>.toSetResultJson() = JSONArray().apply {
        forEach { result ->
            put(JSONObject().apply {
                put("id", result.id)
                put("exerciseTrackingId", result.exerciseTrackingId)
                put("setNumber", result.setNumber)
                put("reps", result.reps)
                put("weight", result.weight)
                put("succeeded", result.succeeded)
                put("isWarmup", result.isWarmup)
            })
        }
    }

    private fun JSONArray?.toWarmupSets(): List<WarmupSetPlan> = buildList {
        if (this@toWarmupSets == null) return@buildList
        repeat(length()) { index ->
            val item = optJSONObject(index) ?: return@repeat
            add(WarmupSetPlan(item.optInt("reps", 1), item.optInt("weightPercent", 0)))
        }
    }

    private fun JSONArray?.toWorkSets(): List<WorkSetPlan> = buildList {
        if (this@toWorkSets == null) return@buildList
        repeat(length()) { index ->
            val item = optJSONObject(index) ?: return@repeat
            add(WorkSetPlan(item.optInt("reps", 1), item.optDouble("weightOffset", 0.0)))
        }
    }

    private fun JSONArray?.toSetResults(): List<WorkoutSetResult> = buildList {
        if (this@toSetResults == null) return@buildList
        repeat(length()) { index ->
            val item = optJSONObject(index) ?: return@repeat
            add(
                WorkoutSetResult(
                    id = item.optString("id"),
                    exerciseTrackingId = item.optString("exerciseTrackingId"),
                    setNumber = item.optInt("setNumber"),
                    reps = item.optInt("reps"),
                    weight = item.optDouble("weight"),
                    succeeded = item.optBoolean("succeeded", true),
                    isWarmup = item.optBoolean("isWarmup", false),
                ),
            )
        }
    }

    private fun JSONArray?.toDoubleList(): List<Double> = buildList {
        if (this@toDoubleList == null) return@buildList
        repeat(length()) { index -> optDouble(index).takeIf { it > 0 }?.let(::add) }
    }.distinct().sortedDescending()

    private companion object { const val KEY_STATE = "app_state" }
}

fun mergePlanImport(state: AppState, imported: PlanImport): AppState {
    val mergedPlans = state.plans.map { existing ->
        imported.plans.firstOrNull { it.id == existing.id } ?: existing
    } + imported.plans.filterNot { it.id in state.plans.map { plan -> plan.id }.toSet() }

    val mergedLibrary = state.exerciseLibrary.map { existing ->
        imported.exerciseLibrary.firstOrNull { it.id == existing.id } ?: existing
    } + imported.exerciseLibrary.filterNot { importedExercise ->
        state.exerciseLibrary.any { it.id == importedExercise.id }
    }

    return state.copy(
        plans = mergedPlans,
        exerciseLibrary = mergedLibrary,
        selectedPlanId = state.selectedPlanId ?: mergedPlans.firstOrNull()?.id,
    )
}
