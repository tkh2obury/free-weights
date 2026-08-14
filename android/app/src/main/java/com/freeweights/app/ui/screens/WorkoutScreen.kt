package com.freeweights.app.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeweights.app.model.ActiveWorkout
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.model.WorkoutSetResult
import com.freeweights.app.ui.PlateLoadPanel
import com.freeweights.app.ui.RestTimerPanel
import com.freeweights.app.ui.SectionHeader
import com.freeweights.app.ui.ThemedChoiceChip
import com.freeweights.app.ui.formatPlate
import com.freeweights.app.ui.formatDuration
import com.freeweights.app.util.calculatePlates
import com.freeweights.app.util.configuredSetWeight
import com.freeweights.app.util.PrescribedLiftSet
import com.freeweights.app.util.prescribedLiftSets
import com.freeweights.app.util.nextWorkoutDayId
import com.freeweights.app.util.suggestedWeight
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun WorkoutScreen(state: AppState, onStateChange: (AppState) -> Unit) {
    val selectedPlan = state.selectedPlan
    var selectedDayId by rememberSaveable(selectedPlan?.id) {
        mutableStateOf(nextWorkoutDayId(selectedPlan, state.logs))
    }
    val selectedDay = selectedPlan?.days?.firstOrNull { it.id == selectedDayId }
        ?: selectedPlan?.days?.firstOrNull()

    val active = state.activeWorkout
    val activePlan = state.plans.firstOrNull { it.id == active?.planId }
    val activeDay = activePlan?.days?.firstOrNull { it.id == active?.dayId }
    val currentExercise = activeDay?.exercises?.getOrNull(active?.currentExerciseIndex ?: -1)

    fun resolvedSetWeight(targetWeight: Double): Double = configuredSetWeight(
        targetWeight = targetWeight,
        barWeight = state.barWeight(),
        availablePlates = state.availablePlates(),
    )

    fun firstSetWeight(exercise: ExercisePlan, logs: List<WorkoutLog>): Double = prescribedLiftSets(
        exercise = exercise,
        workingWeight = suggestedWeight(exercise, logs),
        barWeight = state.barWeight(),
        availablePlates = state.availablePlates(),
    ).firstOrNull()?.weight ?: resolvedSetWeight(suggestedWeight(exercise, logs))

    LaunchedEffect(selectedPlan?.id, selectedPlan?.days?.map { it.id }) {
        if (selectedPlan?.days?.none { it.id == selectedDayId } != false) {
            selectedDayId = nextWorkoutDayId(selectedPlan, state.logs)
        }
    }

    fun saveCurrentExercise() {
        val workout = active ?: return
        val plan = activePlan ?: return
        val day = activeDay ?: return
        val exercise = currentExercise ?: return
        val results = workout.setResults
        val workResults = results.filterNot { it.isWarmup }
        val log = WorkoutLog(
            exerciseId = exercise.trackingId,
            exerciseName = exercise.name,
            completedAt = workout.startedAt,
            sets = if (exercise.type == ExerciseType.RUN_WALK) results.size else workResults.size,
            reps = if (exercise.type == ExerciseType.RUN_WALK) 1 else workResults.maxOfOrNull { it.reps } ?: exercise.targetReps,
            weight = if (exercise.type == ExerciseType.RUN_WALK) 0.0 else workResults.maxOfOrNull { it.weight } ?: workout.currentWeight ?: exercise.workingWeight,
            sessionId = workout.sessionId,
            planId = plan.id,
            planName = plan.name,
            dayId = day.id,
            dayName = day.name,
            failedSets = if (exercise.type == ExerciseType.RUN_WALK) results.count { !it.succeeded } else workResults.count { !it.succeeded },
            exerciseType = exercise.type,
            runSeconds = exercise.runSeconds,
            walkSeconds = exercise.walkSeconds,
            intervalRounds = exercise.intervalRounds,
            setResults = if (exercise.type == ExerciseType.RUN_WALK) emptyList() else results,
        )
        val updatedLogs = state.logs + log
        if (workout.currentExerciseIndex == day.exercises.lastIndex) {
            onStateChange(state.copy(logs = updatedLogs, activeWorkout = null))
            selectedDayId = nextWorkoutDayId(plan, updatedLogs)
        } else {
            val nextIndex = workout.currentExerciseIndex + 1
            val nextExercise = day.exercises[nextIndex]
            onStateChange(
                state.copy(
                    logs = updatedLogs,
                    activeWorkout = workout.copy(
                        currentExerciseIndex = nextIndex,
                        setResults = emptyList(),
                        currentWeight = if (nextExercise.type == ExerciseType.RUN_WALK) {
                            0.0
                        } else {
                            firstSetWeight(nextExercise, updatedLogs)
                        },
                        intervalPhase = null,
                        intervalEndsAt = null,
                        intervalPausedSeconds = null,
                    ),
                ),
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SectionHeader(if (active == null) "Start workout" else "Workout running") }

        if (active == null) {
            if (state.plans.isEmpty()) {
                item { TerminalPanel("NO PLAN SELECTED", "Create a plan in the Plans tab before starting a workout.") }
            } else {
                item {
                    Text("SELECT PLAN", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.plans.forEach { plan ->
                            ThemedChoiceChip(
                                selected = plan.id == selectedPlan?.id,
                                onClick = {
                                    onStateChange(state.copy(selectedPlanId = plan.id))
                                    selectedDayId = nextWorkoutDayId(plan, state.logs)
                                },
                                label = plan.name,
                            )
                        }
                    }
                }

                if (selectedPlan != null) {
                    item {
                        Text("NEXT DAY IS PRESELECTED", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                        Spacer(Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            selectedPlan.days.forEach { day ->
                                ThemedChoiceChip(
                                    selected = day.id == selectedDay?.id,
                                    onClick = { selectedDayId = day.id },
                                    label = day.name,
                                )
                            }
                        }
                    }
                }

                if (selectedDay == null) {
                    item { TerminalPanel("NO DAY SELECTED", "Add a workout day to ${selectedPlan?.name ?: "this plan"}.") }
                } else if (selectedDay.exercises.isEmpty()) {
                    item { TerminalPanel("NO EXERCISES", "Add exercises to ${selectedDay.name} before starting.") }
                } else {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text("${selectedPlan?.name} / ${selectedDay.name}", fontSize = 17.sp, fontWeight = FontWeight.Black)
                                selectedDay.exercises.forEachIndexed { index, exercise ->
                                    Text(
                                        if (exercise.type == ExerciseType.RUN_WALK) {
                                            "${(index + 1).toString().padStart(2, '0')}  ${exercise.name} · ${exercise.intervalRounds}× ${formatDuration(exercise.runSeconds)}/${formatDuration(exercise.walkSeconds)}"
                                        } else {
                                            val workCount = exercise.workSets.size.takeIf { it > 0 } ?: exercise.targetSets
                                            val warmup = exercise.warmupSets.size.takeIf { it > 0 }?.let { " · ${it}W" }.orEmpty()
                                            "${(index + 1).toString().padStart(2, '0')}  ${exercise.name} · $workCount WORK$warmup"
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                val plan = requireNotNull(selectedPlan)
                                val firstExercise = selectedDay.exercises.first()
                                onStateChange(
                                    state.copy(
                                        activeWorkout = ActiveWorkout(
                                            planId = plan.id,
                                            dayId = selectedDay.id,
                                            currentWeight = if (firstExercise.type == ExerciseType.RUN_WALK) {
                                                0.0
                                            } else {
                                                firstSetWeight(firstExercise, state.logs)
                                            },
                                        ),
                                    ),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = CutCornerShape(9.dp),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Text("START ${selectedDay.name.uppercase()}", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        } else if (activePlan == null || activeDay == null || currentExercise == null) {
            item { TerminalPanel("WORKOUT FILE CHANGED", "The active plan or day is no longer available.") }
            item {
                OutlinedButton(
                    onClick = { onStateChange(state.copy(activeWorkout = null)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("EXIT WORKOUT") }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${activePlan.name} / ${activeDay.name}", fontWeight = FontWeight.Bold)
                        Text(
                            "EXERCISE ${active.currentExerciseIndex + 1} OF ${activeDay.exercises.size}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                    }
                    OutlinedButton(
                        onClick = { onStateChange(state.copy(activeWorkout = null)) },
                        shape = CutCornerShape(7.dp),
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                        Text("END")
                    }
                }
            }

            item {
                if (currentExercise.type == ExerciseType.RUN_WALK) {
                    RunWalkIntervalCard(
                        active = active,
                        exercise = currentExercise,
                        exerciseCount = activeDay.exercises.size,
                        onActiveChange = { updated -> onStateChange(state.copy(activeWorkout = updated)) },
                        onSaveExercise = ::saveCurrentExercise,
                    )
                } else {
                ActiveSetCard(
                    state = state,
                    active = active,
                    exercise = currentExercise,
                    exerciseCount = activeDay.exercises.size,
                    onWeightChange = { weight ->
                        onStateChange(state.copy(activeWorkout = active.copy(currentWeight = weight)))
                    },
                    onSetResult = { succeeded, prescribed ->
                        val result = WorkoutSetResult(
                            exerciseTrackingId = currentExercise.trackingId,
                            setNumber = active.setResults.size + 1,
                            reps = prescribed.reps,
                            weight = prescribed.weight,
                            succeeded = succeeded,
                            isWarmup = prescribed.isWarmup,
                        )
                        val updatedResults = active.setResults + result
                        val sequence = prescribedLiftSets(
                            exercise = currentExercise,
                            workingWeight = suggestedWeight(currentExercise, state.logs),
                            barWeight = state.barWeight(),
                            availablePlates = state.availablePlates(),
                        )
                        val nextWeight = sequence.getOrNull(updatedResults.size)?.weight ?: prescribed.weight
                        onStateChange(state.copy(activeWorkout = active.copy(setResults = updatedResults, currentWeight = nextWeight)))
                    },
                    onUndo = { weight ->
                        onStateChange(state.copy(activeWorkout = active.copy(setResults = active.setResults.dropLast(1), currentWeight = weight)))
                    },
                    onSaveExercise = ::saveCurrentExercise,
                )
                }
            }
        }
    }
}

@Composable
private fun RunWalkIntervalCard(
    active: ActiveWorkout,
    exercise: ExercisePlan,
    exerciseCount: Int,
    onActiveChange: (ActiveWorkout) -> Unit,
    onSaveExercise: () -> Unit,
) {
    val phase = active.intervalPhase ?: "RUN"
    val completed = active.setResults.size
    val running = active.intervalEndsAt != null
    val phaseDuration = if (phase == "RUN") exercise.runSeconds else exercise.walkSeconds
    var now by remember(active.sessionId, active.currentExerciseIndex) { mutableLongStateOf(System.currentTimeMillis()) }
    val toneGenerator = remember(active.sessionId, active.currentExerciseIndex) {
        runCatching { ToneGenerator(AudioManager.STREAM_ALARM, 90) }.getOrNull()
    }
    DisposableEffect(toneGenerator) {
        onDispose { toneGenerator?.release() }
    }
    fun beep() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 450)
    }
    val remaining = if (active.intervalEndsAt != null) {
        ceil((active.intervalEndsAt - now).coerceAtLeast(0L) / 1000.0).toInt()
    } else {
        active.intervalPausedSeconds ?: phaseDuration
    }

    LaunchedEffect(active.intervalEndsAt, phase, completed) {
        val end = active.intervalEndsAt ?: return@LaunchedEffect
        while (System.currentTimeMillis() < end) {
            now = System.currentTimeMillis()
            delay(200)
        }
        now = System.currentTimeMillis()
        beep()
        onActiveChange(advanceRunWalkPhase(active, exercise, System.currentTimeMillis()))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(exercise.name, color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(
                "$completed OF ${exercise.intervalRounds} INTERVALS COMPLETE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
            if (completed < exercise.intervalRounds) {
                Text(phase, color = if (phase == "RUN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, fontSize = 17.sp, fontWeight = FontWeight.Black)
                Text(formatDuration(remaining), fontSize = 56.sp, fontWeight = FontWeight.Black)
                Text(
                    "RUN ${formatDuration(exercise.runSeconds)}  |  WALK ${formatDuration(exercise.walkSeconds)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Button(
                    onClick = {
                        if (running) {
                            onActiveChange(active.copy(intervalEndsAt = null, intervalPausedSeconds = remaining.coerceAtLeast(1)))
                        } else {
                            onActiveChange(
                                active.copy(
                                    intervalPhase = phase,
                                    intervalEndsAt = System.currentTimeMillis() + remaining.coerceAtLeast(1) * 1000L,
                                    intervalPausedSeconds = null,
                                ),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(8.dp),
                ) {
                    Icon(if (running) Icons.Rounded.Close else Icons.Rounded.PlayArrow, contentDescription = null)
                    Text(if (running) "PAUSE" else if (remaining == phaseDuration) "START $phase" else "RESUME $phase", fontWeight = FontWeight.Black)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(
                        onClick = { onActiveChange(previousRunWalkInterval(active, exercise)) },
                        modifier = Modifier.weight(1f),
                        enabled = phase == "WALK" || completed > 0,
                        shape = CutCornerShape(8.dp),
                    ) {
                        Text("PREVIOUS INTERVAL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            beep()
                            onActiveChange(skipRunWalkInterval(active, exercise))
                        },
                        modifier = Modifier.weight(1f),
                        shape = CutCornerShape(8.dp),
                    ) {
                        Text("SKIP INTERVAL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text("INTERVALS COMPLETE", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                OutlinedButton(
                    onClick = { onActiveChange(previousRunWalkInterval(active, exercise)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(8.dp),
                ) {
                    Text("PREVIOUS INTERVAL", fontWeight = FontWeight.Bold)
                }
                Button(onClick = onSaveExercise, modifier = Modifier.fillMaxWidth(), shape = CutCornerShape(8.dp)) {
                    Text(
                        if (active.currentExerciseIndex == exerciseCount - 1) "FINISH + SAVE WORKOUT" else "SAVE + NEXT EXERCISE",
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

internal fun advanceRunWalkPhase(active: ActiveWorkout, exercise: ExercisePlan, now: Long): ActiveWorkout {
    val phase = active.intervalPhase ?: "RUN"
    if (phase == "RUN") {
        return active.copy(
            intervalPhase = "WALK",
            intervalEndsAt = now + exercise.walkSeconds * 1000L,
            intervalPausedSeconds = null,
        )
    }
    val result = WorkoutSetResult(
        exerciseTrackingId = exercise.trackingId,
        setNumber = active.setResults.size + 1,
        reps = 1,
        weight = 0.0,
        succeeded = true,
    )
    val results = active.setResults + result
    return active.copy(
        setResults = results,
        intervalPhase = "RUN",
        intervalEndsAt = if (results.size < exercise.intervalRounds) now + exercise.runSeconds * 1000L else null,
        intervalPausedSeconds = if (results.size < exercise.intervalRounds) null else exercise.runSeconds,
    )
}

internal fun skipRunWalkInterval(active: ActiveWorkout, exercise: ExercisePlan): ActiveWorkout {
    if (active.setResults.size >= exercise.intervalRounds) return active
    val result = WorkoutSetResult(
        exerciseTrackingId = exercise.trackingId,
        setNumber = active.setResults.size + 1,
        reps = 1,
        weight = 0.0,
        succeeded = true,
    )
    return active.copy(
        setResults = active.setResults + result,
        intervalPhase = "RUN",
        intervalEndsAt = null,
        intervalPausedSeconds = exercise.runSeconds,
    )
}

internal fun previousRunWalkInterval(active: ActiveWorkout, exercise: ExercisePlan): ActiveWorkout {
    val returnToCurrentRun = (active.intervalPhase ?: "RUN") == "WALK"
    val results = if (returnToCurrentRun) active.setResults else active.setResults.dropLast(1)
    return active.copy(
        setResults = results,
        intervalPhase = "RUN",
        intervalEndsAt = null,
        intervalPausedSeconds = exercise.runSeconds,
    )
}

@Composable
private fun ActiveSetCard(
    state: AppState,
    active: ActiveWorkout,
    exercise: ExercisePlan,
    exerciseCount: Int,
    onWeightChange: (Double) -> Unit,
    onSetResult: (Boolean, PrescribedLiftSet) -> Unit,
    onUndo: (Double) -> Unit,
    onSaveExercise: () -> Unit,
) {
    val baseWorkingWeight = suggestedWeight(exercise, state.logs)
    val prescribedSets = prescribedLiftSets(
        exercise = exercise,
        workingWeight = baseWorkingWeight,
        barWeight = state.barWeight(),
        availablePlates = state.availablePlates(),
    )
    val attemptedSets = active.setResults.size
    val currentSet = prescribedSets.getOrNull(attemptedSets)
    var weightText by remember(active.sessionId, active.currentExerciseIndex, attemptedSets, active.currentWeight) {
        mutableStateOf(formatPlate(currentSet?.weight ?: active.currentWeight ?: baseWorkingWeight))
    }
    val weight = weightText.toDoubleOrNull()
    val failedSets = active.setResults.count { !it.succeeded && !it.isWarmup }
    val load = calculatePlates(
        targetWeight = weight ?: 0.0,
        barWeight = state.barWeight(),
        availablePlates = state.availablePlates(),
    )

    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(exercise.name, color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(
                    currentSet?.let {
                        val group = if (it.isWarmup) "WARM-UP" else "WORK SET"
                        "$group ${it.groupIndex + 1} OF ${it.groupCount}  |  ${it.reps} REPS  |  $failedSets FAILED"
                    } ?: "${prescribedSets.size} SETS COMPLETE  |  $failedSets FAILED",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    prescribedSets.forEachIndexed { index, prescribed ->
                        val result = active.setResults.getOrNull(index)
                        val color = when {
                            result?.succeeded == true -> MaterialTheme.colorScheme.primary
                            result?.succeeded == false -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Surface(
                            shape = CutCornerShape(5.dp),
                            color = color.copy(alpha = .13f),
                            border = BorderStroke(1.dp, color),
                        ) {
                            Text(
                                "${if (prescribed.isWarmup) "W" else "S"}${prescribed.groupIndex + 1} ${when (result?.succeeded) { true -> "✓"; false -> "×"; null -> "·" }}",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { next ->
                        if (next.isEmpty() || next.all { it.isDigit() || it == '.' }) {
                            weightText = next
                            next.toDoubleOrNull()?.let(onWeightChange)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("CURRENT SET WEIGHT (${state.unit.label.uppercase()})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                PlateLoadPanel(load, state.unit)

                if (currentSet != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Button(
                            onClick = {
                                if (weight != null) onSetResult(true, currentSet.copy(weight = load.loadedWeight))
                            },
                            modifier = Modifier.weight(1f),
                            enabled = weight != null,
                            shape = CutCornerShape(8.dp),
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                            Text("COMPLETE", fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = {
                                if (weight != null) onSetResult(false, currentSet.copy(weight = load.loadedWeight))
                            },
                            modifier = Modifier.weight(1f),
                            enabled = weight != null,
                            shape = CutCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null)
                            Text("FAILED", fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    Button(
                        onClick = onSaveExercise,
                        modifier = Modifier.fillMaxWidth(),
                        shape = CutCornerShape(8.dp),
                    ) {
                        Text(
                            if (active.currentExerciseIndex == exerciseCount - 1) "FINISH + SAVE WORKOUT" else "SAVE + NEXT EXERCISE",
                            fontWeight = FontWeight.Black,
                        )
                    }
                }

                if (active.setResults.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val previousIndex = (attemptedSets - 1).coerceAtLeast(0)
                            onUndo(prescribedSets.getOrNull(previousIndex)?.weight ?: baseWorkingWeight)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CutCornerShape(7.dp),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Text("UNDO LAST SET")
                    }
                }
            }
        }

        key(exercise.id) {
            RestTimerPanel(initialDuration = exercise.restSeconds, title = "REST / ${exercise.name.uppercase()}")
        }
    }
}

@Composable
private fun TerminalPanel(title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(7.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
