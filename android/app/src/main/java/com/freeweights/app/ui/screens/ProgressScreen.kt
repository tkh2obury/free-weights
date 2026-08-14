package com.freeweights.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WeightUnit
import com.freeweights.app.model.WorkoutLog
import com.freeweights.app.ui.MetricCard
import com.freeweights.app.ui.SectionHeader
import com.freeweights.app.ui.formatDuration
import com.freeweights.app.ui.formatWeight
import com.freeweights.app.util.personalBest
import com.freeweights.app.util.totalVolume
import com.freeweights.app.util.deleteSession
import com.freeweights.app.util.sessionKey
import com.freeweights.app.util.runWalkDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(state: AppState, onStateChange: (AppState) -> Unit) {
    val latestLog = state.logs.maxByOrNull { it.completedAt }
    var selectedId by rememberSaveable { mutableStateOf(latestLog?.exerciseId.orEmpty()) }
    val selectedLogs = state.logs.filter { it.exerciseId == selectedId }.sortedBy { it.completedAt }
    val selectedName = selectedLogs.lastOrNull()?.exerciseName
        ?: state.plans.flatMap { it.days }.flatMap { it.exercises }.firstOrNull { it.trackingId == selectedId }?.name
    val selectedIsRunWalk = selectedLogs.lastOrNull()?.exerciseType == ExerciseType.RUN_WALK
    val best = personalBest(state.logs)
    var editingLog by remember { mutableStateOf<WorkoutLog?>(null) }
    var deletingSession by remember { mutableStateOf<List<WorkoutLog>?>(null) }
    val sessions = state.logs
        .groupBy(::sessionKey)
        .values
        .sortedByDescending { logs -> logs.maxOf { it.completedAt } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SectionHeader("Progress") }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                val accent = MaterialTheme.colorScheme.primary
                MetricCard("Sessions", state.logs.map { log -> log.sessionId.ifBlank { log.id } }.distinct().size.toString(), accent, Modifier.weight(1f))
                MetricCard("Volume", compactNumber(totalVolume(state.logs)), accent, Modifier.weight(1f))
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("ALL-TIME TOP WEIGHT", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        best?.let { "${formatWeight(it.weight, state.unit)} / ${it.exerciseName}" } ?: "NO WORKOUT DATA",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(Modifier.padding(17.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(selectedName ?: "SELECT A SESSION", fontWeight = FontWeight.Bold)
                            Text(
                                if (selectedIsRunWalk) "TOTAL SESSION DURATION" else "WORKING WEIGHT / SESSION",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                            )
                        }
                        if (selectedLogs.isNotEmpty()) {
                            Text(
                                if (selectedIsRunWalk) {
                                    formatDuration(runWalkDuration(selectedLogs.last()).totalSeconds)
                                } else {
                                    formatWeight(selectedLogs.maxOf { it.weight }, state.unit)
                                },
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    Spacer(Modifier.height(17.dp))
                    if (selectedIsRunWalk) {
                        RunWalkProgressChart(selectedLogs)
                    } else {
                        ProgressChart(selectedLogs, state.unit)
                    }
                    if (selectedLogs.size < 2) {
                        Text(
                            "Complete this exercise twice to draw a trend.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        item { Text("RECENT SESSION FOLDERS", fontSize = 15.sp, fontWeight = FontWeight.Black) }

        items(sessions, key = { logs -> logs.first().sessionId.ifBlank { logs.first().id } }) { logs ->
            SessionFolder(
                logs = logs,
                state = state,
                selectedExerciseId = selectedId,
                onSelectExercise = { selectedId = it },
                onEdit = { editingLog = it },
                onDelete = { deletingSession = logs },
            )
        }

        if (sessions.isEmpty()) {
            item {
                Text("Completed workouts will appear here as session folders.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    editingLog?.let { log ->
        EditSessionDialog(
            log = log,
            unitLabel = state.unit.label,
            onDismiss = { editingLog = null },
            onSave = { updated ->
                onStateChange(state.copy(logs = state.logs.map { if (it.id == updated.id) updated else it }))
                selectedId = updated.exerciseId
                editingLog = null
            },
        )
    }

    deletingSession?.let { logs ->
        val newest = logs.maxBy { it.completedAt }
        AlertDialog(
            onDismissRequest = { deletingSession = null },
            title = { Text("Delete session?") },
            text = {
                Text(
                    "Delete ${newest.planName} / ${newest.dayName} and all ${logs.size} exercise entries in this session? This cannot be undone.",
                )
            },
            dismissButton = { TextButton(onClick = { deletingSession = null }) { Text("Cancel") } },
            confirmButton = {
                Button(
                    onClick = {
                        val remaining = deleteSession(state.logs, sessionKey(newest))
                        onStateChange(state.copy(logs = remaining))
                        if (remaining.none { it.exerciseId == selectedId }) {
                            selectedId = remaining.maxByOrNull { it.completedAt }?.exerciseId.orEmpty()
                        }
                        deletingSession = null
                    },
                ) { Text("Delete") }
            },
        )
    }
}

@Composable
private fun ProgressChart(logs: List<WorkoutLog>, unit: WeightUnit) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = .55f)
    val recent = logs.takeLast(12)
    var selectedIndex by remember(recent) { mutableIntStateOf(-1) }
    var chartWidth by remember { mutableFloatStateOf(1f) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    fun selectPoint(x: Float) {
        if (recent.isNotEmpty()) {
            selectedIndex = if (recent.size == 1) 0 else {
                ((x.coerceIn(0f, chartWidth) / chartWidth) * recent.lastIndex).roundToInt().coerceIn(0, recent.lastIndex)
            }
        }
    }

    LaunchedEffect(recent) {
        if (selectedIndex !in recent.indices) selectedIndex = -1
    }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            if (selectedIndex in recent.indices) {
                val selected = recent[selectedIndex]
                "${dateFormat.format(Date(selected.completedAt)).uppercase()}  |  ${formatWeight(selected.weight, unit)}"
            } else {
                "TAP OR DRAG ACROSS A POINT FOR WEIGHT + DATE"
            },
            color = if (selectedIndex in recent.indices) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(MaterialTheme.colorScheme.background, CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp))
            .padding(13.dp)
            .onSizeChanged { chartWidth = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(recent, chartWidth) { detectTapGestures { selectPoint(it.x) } }
            .pointerInput(recent, chartWidth) {
                detectDragGestures(
                    onDragStart = { selectPoint(it.x) },
                    onDrag = { change, _ -> selectPoint(change.position.x) },
                )
            },
    ) {
        repeat(5) { index ->
            val y = size.height * index / 4f
            drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        repeat(7) { index ->
            val x = size.width * index / 6f
            drawLine(grid.copy(alpha = .28f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
        }
        if (recent.isEmpty()) return@Canvas
        val min = recent.minOf { it.weight }
        val max = recent.maxOf { it.weight }
        val range = (max - min).coerceAtLeast(1.0)
        val path = Path()
        recent.forEachIndexed { index, log ->
            val x = if (recent.size == 1) size.width / 2f else size.width * index / recent.lastIndex.toFloat()
            val y = size.height - ((log.weight - min) / range * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Square))
        recent.forEachIndexed { index, log ->
            val x = if (recent.size == 1) size.width / 2f else size.width * index / recent.lastIndex.toFloat()
            val y = size.height - ((log.weight - min) / range * size.height).toFloat()
            val half = if (index == selectedIndex) 7.dp.toPx() else 4.dp.toPx()
            drawRect(color, topLeft = Offset(x - half, y - half), size = androidx.compose.ui.geometry.Size(half * 2, half * 2))
        }
    }
    }
}

@Composable
private fun RunWalkProgressChart(logs: List<WorkoutLog>) {
    val runColor = MaterialTheme.colorScheme.primary
    val walkColor = MaterialTheme.colorScheme.secondary
    val selectedOutline = MaterialTheme.colorScheme.onSurface
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = .55f)
    val recent = logs.takeLast(12)
    var selectedIndex by remember(recent) { mutableIntStateOf(recent.lastIndex) }
    var chartWidth by remember { mutableFloatStateOf(1f) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    fun selectBar(x: Float) {
        if (recent.isNotEmpty()) {
            selectedIndex = ((x.coerceIn(0f, chartWidth) / chartWidth) * recent.size)
                .toInt()
                .coerceIn(0, recent.lastIndex)
        }
    }

    LaunchedEffect(recent) {
        if (selectedIndex !in recent.indices) selectedIndex = recent.lastIndex
    }

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if (selectedIndex in recent.indices) {
            val selected = recent[selectedIndex]
            val duration = runWalkDuration(selected)
            Text(
                "${dateFormat.format(Date(selected.completedAt)).uppercase()}  |  TOTAL ${formatDuration(duration.totalSeconds)}",
                color = runColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${duration.rounds} INTERVALS  |  EACH ${formatDuration(duration.intervalSeconds)}  |  RUN ${formatDuration(duration.runSecondsPerInterval)}  |  WALK ${formatDuration(duration.walkSecondsPerInterval)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                "TAP OR DRAG ACROSS A BAR FOR DURATION + DATE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .background(MaterialTheme.colorScheme.background, CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp))
                .padding(13.dp)
                .onSizeChanged { chartWidth = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(recent, chartWidth) { detectTapGestures { selectBar(it.x) } }
                .pointerInput(recent, chartWidth) {
                    detectDragGestures(
                        onDragStart = { selectBar(it.x) },
                        onDrag = { change, _ -> selectBar(change.position.x) },
                    )
                },
        ) {
            repeat(5) { index ->
                val y = size.height * index / 4f
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            if (recent.isEmpty()) return@Canvas
            val durations = recent.map(::runWalkDuration)
            val maxTotal = durations.maxOf { it.totalSeconds }.coerceAtLeast(1)
            val slotWidth = size.width / recent.size
            val barWidth = (slotWidth * .56f).coerceAtMost(32.dp.toPx())
            durations.forEachIndexed { index, duration ->
                val x = slotWidth * index + (slotWidth - barWidth) / 2f
                val runHeight = size.height * duration.runSeconds / maxTotal.toFloat()
                val walkHeight = size.height * duration.walkSeconds / maxTotal.toFloat()
                val runTop = size.height - runHeight
                drawRect(
                    color = runColor,
                    topLeft = Offset(x, runTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, runHeight),
                )
                drawRect(
                    color = walkColor,
                    topLeft = Offset(x, runTop - walkHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, walkHeight),
                )
                if (index == selectedIndex) {
                    drawRect(
                        color = selectedOutline,
                        topLeft = Offset(x - 2.dp.toPx(), runTop - walkHeight - 2.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(barWidth + 4.dp.toPx(), runHeight + walkHeight + 4.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("■ RUN", color = runColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("■ WALK", color = walkColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SessionFolder(
    logs: List<WorkoutLog>,
    state: AppState,
    selectedExerciseId: String,
    onSelectExercise: (String) -> Unit,
    onEdit: (WorkoutLog) -> Unit,
    onDelete: () -> Unit,
) {
    val ordered = logs.sortedBy { it.exerciseName }
    val newest = logs.maxBy { it.completedAt }
    var expanded by rememberSaveable(newest.sessionId) { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yy h:mm a", Locale.getDefault()) }
    val sessionVolume = logs.sumOf { it.volume }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                        ordered.firstOrNull()?.let { onSelectExercise(it.exerciseId) }
                    }
                    .padding(horizontal = 11.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${newest.planName} / ${newest.dayName}",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${dateFormat.format(Date(newest.completedAt))} · ${logs.size} EX · VOL ${compactNumber(sessionVolume)} ${state.unit.label.uppercase()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete session")
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            if (expanded) {
                ordered.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectExercise(log.exerciseId) }
                            .background(if (selectedExerciseId == log.exerciseId) MaterialTheme.colorScheme.primary.copy(alpha = .1f) else MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(log.exerciseName, color = if (selectedExerciseId == log.exerciseId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(
                                if (log.exerciseType == ExerciseType.RUN_WALK) {
                                    "${log.intervalRounds.coerceAtLeast(log.sets)} INTERVALS  |  RUN ${log.runSeconds}s  |  WALK ${log.walkSeconds}s"
                                } else if (log.setResults.isNotEmpty()) {
                                    val workSets = log.setResults.filterNot { it.isWarmup }
                                    "${workSets.size} SETS  |  TOP ${formatWeight(workSets.maxOfOrNull { it.weight } ?: log.weight, state.unit)}${if (log.failedSets > 0) "  |  ${log.failedSets} FAILED" else ""}"
                                } else {
                                    "${log.sets} × ${log.reps}  |  ${formatWeight(log.weight, state.unit)}${if (log.failedSets > 0) "  |  ${log.failedSets} FAILED" else ""}"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                            )
                        }
                        Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = "Show exercise graph", tint = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { onEdit(log) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit session")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditSessionDialog(
    log: WorkoutLog,
    unitLabel: String,
    onDismiss: () -> Unit,
    onSave: (WorkoutLog) -> Unit,
) {
    var sets by remember(log.id) { mutableStateOf(log.sets.toString()) }
    var reps by remember(log.id) { mutableStateOf(log.reps.toString()) }
    var weight by remember(log.id) { mutableStateOf(log.weight.toString()) }
    var failed by remember(log.id) { mutableStateOf(log.failedSets.toString()) }
    var intervalRounds by remember(log.id) { mutableStateOf(log.intervalRounds.coerceAtLeast(log.sets).toString()) }
    var runSeconds by remember(log.id) { mutableStateOf(log.runSeconds.coerceAtLeast(1).toString()) }
    var walkSeconds by remember(log.id) { mutableStateOf(log.walkSeconds.coerceAtLeast(1).toString()) }
    val setsValue = sets.toIntOrNull()
    val repsValue = reps.toIntOrNull()
    val weightValue = weight.toDoubleOrNull()
    val failedValue = failed.toIntOrNull()
    val intervalRoundsValue = intervalRounds.toIntOrNull()
    val runSecondsValue = runSeconds.toIntOrNull()
    val walkSecondsValue = walkSeconds.toIntOrNull()
    val valid = if (log.exerciseType == ExerciseType.RUN_WALK) {
        intervalRoundsValue != null && intervalRoundsValue > 0 &&
            runSecondsValue != null && runSecondsValue > 0 &&
            walkSecondsValue != null && walkSecondsValue > 0
    } else {
        setsValue != null && setsValue > 0 &&
            repsValue != null && repsValue > 0 &&
            weightValue != null && weightValue >= 0 &&
            failedValue != null && failedValue in 0..setsValue
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${log.exerciseName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (log.exerciseType == ExerciseType.RUN_WALK) {
                    SessionNumberField(intervalRounds, { intervalRounds = it }, "INTERVALS", Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        SessionNumberField(runSeconds, { runSeconds = it }, "RUN SECONDS", Modifier.weight(1f))
                        SessionNumberField(walkSeconds, { walkSeconds = it }, "WALK SECONDS", Modifier.weight(1f))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        SessionNumberField(sets, { sets = it }, "SETS", Modifier.weight(1f))
                        SessionNumberField(reps, { reps = it }, "REPS", Modifier.weight(1f))
                    }
                    SessionNumberField(weight, { weight = it }, "WEIGHT ${unitLabel.uppercase()}", Modifier.fillMaxWidth(), decimal = true)
                    SessionNumberField(failed, { failed = it }, "FAILED SETS", Modifier.fillMaxWidth())
                    Text("Failed sets must be between 0 and total sets.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        if (log.exerciseType == ExerciseType.RUN_WALK) {
                            log.copy(
                                sets = requireNotNull(intervalRoundsValue),
                                intervalRounds = intervalRoundsValue,
                                runSeconds = requireNotNull(runSecondsValue),
                                walkSeconds = requireNotNull(walkSecondsValue),
                            )
                        } else {
                            log.copy(
                                sets = requireNotNull(setsValue),
                                reps = requireNotNull(repsValue),
                                weight = requireNotNull(weightValue),
                                failedSets = requireNotNull(failedValue),
                                setResults = emptyList(),
                            )
                        },
                    )
                },
                enabled = valid,
            ) { Text("Save") }
        },
    )
}

@Composable
private fun SessionNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    decimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (next.isEmpty() || next.all { it.isDigit() || decimal && it == '.' }) onValueChange(next)
        },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
    )
}

private fun compactNumber(value: Double): String = when {
    value >= 1_000_000 -> "${(value / 100_000).roundToInt() / 10.0}m"
    value >= 1_000 -> "${(value / 100).roundToInt() / 10.0}k"
    else -> value.roundToInt().toString()
}
