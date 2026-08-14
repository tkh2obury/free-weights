package com.freeweights.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseDefinition
import com.freeweights.app.model.ExercisePlan
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.model.WorkoutDay
import com.freeweights.app.model.WorkoutPlan
import com.freeweights.app.model.WarmupSetPlan
import com.freeweights.app.model.WorkSetPlan
import com.freeweights.app.ui.SectionHeader
import com.freeweights.app.ui.ThemedChoiceChip
import com.freeweights.app.ui.formatDuration
import com.freeweights.app.ui.formatWeight
import com.freeweights.app.util.deleteLibraryExercise
import com.freeweights.app.util.filteredExerciseLibrary
import com.freeweights.app.util.renameLibraryExercise
import com.freeweights.app.util.suggestedWeight
import java.util.UUID

@Composable
fun PlanScreen(state: AppState, onStateChange: (AppState) -> Unit) {
    val selectedPlan = state.selectedPlan
    var selectedDayId by rememberSaveable(selectedPlan?.id) {
        mutableStateOf(selectedPlan?.days?.firstOrNull()?.id.orEmpty())
    }
    var planDialog by remember { mutableStateOf<WorkoutPlan?>(null) }
    var creatingPlan by remember { mutableStateOf(false) }
    var deletePlan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var showDayDialog by remember { mutableStateOf(false) }
    var deleteDay by remember { mutableStateOf<WorkoutDay?>(null) }
    var renameDay by remember { mutableStateOf<WorkoutDay?>(null) }
    var showExerciseDialog by remember { mutableStateOf(false) }
    var plansExpanded by rememberSaveable { mutableStateOf(false) }
    var exerciseLibraryExpanded by rememberSaveable { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<ExercisePlan?>(null) }
    val selectedDay = selectedPlan?.days?.firstOrNull { it.id == selectedDayId }
        ?: selectedPlan?.days?.firstOrNull()

    LaunchedEffect(selectedPlan?.id, selectedPlan?.days?.map { it.id }) {
        if (selectedPlan?.days?.none { it.id == selectedDayId } != false) {
            selectedDayId = selectedPlan?.days?.firstOrNull()?.id.orEmpty()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionHeader(
                title = "Plan library",
                trailing = {
                    OutlinedButton(
                        onClick = { creatingPlan = true },
                        shape = CutCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("PLAN", maxLines = 1, softWrap = false)
                    }
                },
            )
        }

        if (state.plans.isNotEmpty()) {
            item {
                PlanPickerSection(
                    plans = state.plans,
                    selectedPlan = selectedPlan,
                    expanded = plansExpanded,
                    onToggle = { plansExpanded = !plansExpanded },
                    onSelect = { plan ->
                        onStateChange(state.copy(selectedPlanId = plan.id))
                        plansExpanded = false
                    },
                    onEdit = { planDialog = it },
                    onDelete = { deletePlan = it },
                )
            }
        }

        item {
            ExerciseLibrarySection(
                state = state,
                expanded = exerciseLibraryExpanded,
                onToggle = { exerciseLibraryExpanded = !exerciseLibraryExpanded },
                onRename = { exerciseId, name ->
                    onStateChange(renameLibraryExercise(state, exerciseId, name))
                },
                onDelete = { exerciseId ->
                    onStateChange(deleteLibraryExercise(state, exerciseId))
                },
            )
        }

        if (selectedPlan == null) {
            item {
                EmptyPanel(
                    title = "NO PLAN FILES",
                    message = "Create a plan, then add workout days and exercises.",
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(selectedPlan.name, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text(
                            "${selectedPlan.days.size} DAY FILES",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                    }
                    OutlinedButton(
                        onClick = { showDayDialog = true },
                        shape = CutCornerShape(7.dp),
                        contentPadding = PaddingValues(horizontal = 11.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Text("DAY", maxLines = 1, softWrap = false)
                    }
                }
            }

            if (selectedPlan.days.isNotEmpty()) {
                item {
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
                item { EmptyPanel("NO DAY FILES", "Add a day to this plan.") }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    selectedDay.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "${selectedDay.exercises.size} EXERCISES",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                )
                            }
                            IconButton(onClick = { renameDay = selectedDay }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Rename day")
                            }
                            IconButton(onClick = { deleteDay = selectedDay }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete day")
                            }
                        }
                        Button(
                            onClick = { showExerciseDialog = true },
                            shape = CutCornerShape(7.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Text("EXERCISE", maxLines = 1, softWrap = false)
                        }
                    }
                }

                items(selectedDay.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        state = state,
                        onEdit = { editingExercise = exercise },
                        onDelete = {
                            val updatedDay = selectedDay.copy(
                                exercises = selectedDay.exercises.filterNot { it.id == exercise.id },
                            )
                            onStateChange(
                                state.copy(
                                    plans = state.plans.map { plan ->
                                        if (plan.id == selectedPlan.id) plan.copy(
                                            days = plan.days.map { if (it.id == selectedDay.id) updatedDay else it },
                                        ) else plan
                                    },
                                ),
                            )
                        },
                    )
                }

                if (selectedDay.exercises.isEmpty()) {
                    item { EmptyPanel("NO EXERCISES", "Add an exercise and set its rest period.") }
                }
            }
        }
    }

    if (creatingPlan) {
        NameDialog(
            title = "Create plan",
            label = "Plan name",
            initial = "",
            confirmLabel = "Create",
            onDismiss = { creatingPlan = false },
            onSave = { name ->
                val plan = WorkoutPlan(name = name)
                onStateChange(state.copy(plans = state.plans + plan, selectedPlanId = plan.id))
                creatingPlan = false
            },
        )
    }

    planDialog?.let { plan ->
        NameDialog(
            title = "Rename plan",
            label = "Plan name",
            initial = plan.name,
            confirmLabel = "Save",
            onDismiss = { planDialog = null },
            onSave = { name ->
                onStateChange(state.copy(plans = state.plans.map { if (it.id == plan.id) it.copy(name = name) else it }))
                planDialog = null
            },
        )
    }

    deletePlan?.let { plan ->
        DeleteDialog(
            title = "Delete ${plan.name}?",
            message = "This removes the plan and all of its day files. Logged workout history stays in Progress.",
            onDismiss = { deletePlan = null },
            onDelete = {
                val remaining = state.plans.filterNot { it.id == plan.id }
                onStateChange(state.copy(plans = remaining, selectedPlanId = remaining.firstOrNull()?.id))
                deletePlan = null
            },
        )
    }

    if (showDayDialog && selectedPlan != null) {
        NameDialog(
            title = "Add workout day",
            label = "Day name",
            initial = "",
            confirmLabel = "Add",
            onDismiss = { showDayDialog = false },
            onSave = { name ->
                val day = WorkoutDay(name = name)
                onStateChange(
                    state.copy(plans = state.plans.map { if (it.id == selectedPlan.id) it.copy(days = it.days + day) else it }),
                )
                selectedDayId = day.id
                showDayDialog = false
            },
        )
    }

    deleteDay?.let { day ->
        if (selectedPlan != null) {
            DeleteDialog(
                title = "Delete ${day.name}?",
                message = "This removes the day and its exercises from ${selectedPlan.name}.",
                onDismiss = { deleteDay = null },
                onDelete = {
                    val remainingDays = selectedPlan.days.filterNot { it.id == day.id }
                    selectedDayId = remainingDays.firstOrNull()?.id.orEmpty()
                    onStateChange(
                        state.copy(plans = state.plans.map { if (it.id == selectedPlan.id) it.copy(days = remainingDays) else it }),
                    )
                    deleteDay = null
                },
            )
        }
    }

    renameDay?.let { day ->
        if (selectedPlan != null) {
            NameDialog(
                title = "Rename day",
                label = "Day name",
                initial = day.name,
                confirmLabel = "Save",
                onDismiss = { renameDay = null },
                onSave = { name ->
                    onStateChange(
                        state.copy(
                            plans = state.plans.map { plan ->
                                if (plan.id == selectedPlan.id) {
                                    plan.copy(days = plan.days.map { if (it.id == day.id) it.copy(name = name) else it })
                                } else plan
                            },
                        ),
                    )
                    renameDay = null
                },
            )
        }
    }

    if (showExerciseDialog && selectedPlan != null && selectedDay != null) {
        ExerciseDialog(
            title = "Add exercise",
            initial = null,
            existingExercises = state.exerciseLibrary,
            unitLabel = state.unit.label,
            onDismiss = { showExerciseDialog = false },
            onSave = { exercise ->
                val definition = exercise.toDefinition()
                val nextLibrary = if (state.exerciseLibrary.any { it.id == definition.id }) {
                    state.exerciseLibrary.map { if (it.id == definition.id) definition else it }
                } else {
                    state.exerciseLibrary + definition
                }
                onStateChange(
                    state.copy(
                        exerciseLibrary = nextLibrary,
                        plans = state.plans.map { plan ->
                            if (plan.id == selectedPlan.id) plan.copy(
                                days = plan.days.map { day ->
                                    if (day.id == selectedDay.id) day.copy(exercises = day.exercises + exercise) else day
                                },
                            ) else plan
                        },
                    ),
                )
                showExerciseDialog = false
            },
        )
    }

    editingExercise?.let { initial ->
        if (selectedPlan != null && selectedDay != null) {
            ExerciseDialog(
                title = "Edit exercise",
                initial = initial,
                existingExercises = emptyList(),
                unitLabel = state.unit.label,
                onDismiss = { editingExercise = null },
                onSave = { updated ->
                    val definition = updated.toDefinition()
                    onStateChange(
                        state.copy(
                            exerciseLibrary = state.exerciseLibrary.map { if (it.id == definition.id) definition else it },
                            plans = state.plans.map { plan ->
                                plan.copy(
                                    days = plan.days.map { day ->
                                        day.copy(
                                            exercises = day.exercises.map { exercise ->
                                                when {
                                                    exercise.id == updated.id -> updated
                                                    exercise.trackingId == updated.trackingId -> exercise.copy(name = updated.name)
                                                    else -> exercise
                                                }
                                            },
                                        )
                                    },
                                )
                            },
                        ),
                    )
                    editingExercise = null
                },
            )
        }
    }

}

@Composable
private fun PlanPickerSection(
    plans: List<WorkoutPlan>,
    selectedPlan: WorkoutPlan?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (WorkoutPlan) -> Unit,
    onEdit: (WorkoutPlan) -> Unit,
    onDelete: (WorkoutPlan) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("PLANS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    Text(
                        selectedPlan?.name ?: "NO PLAN SELECTED",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text("${plans.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse plans" else "Expand plans",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (expanded) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(plans, key = { it.id }) { plan ->
                        PlanFolder(
                            plan = plan,
                            selected = plan.id == selectedPlan?.id,
                            onSelect = { onSelect(plan) },
                            onEdit = { onEdit(plan) },
                            onDelete = { onDelete(plan) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseLibrarySection(
    state: AppState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var renaming by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var deleting by remember { mutableStateOf<ExerciseDefinition?>(null) }
    val filtered = filteredExerciseLibrary(state.exerciseLibrary, query)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("LIFTS", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text("${state.exerciseLibrary.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse lifts" else "Expand lifts",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("EXERCISE LIBRARY", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${state.exerciseLibrary.size} TOTAL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search exercises") },
                    singleLine = true,
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(filtered, key = { it.id }) { exercise ->
                        val useCount = state.plans.sumOf { plan ->
                            plan.days.sumOf { day -> day.exercises.count { it.trackingId == exercise.id } }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = CutCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(exercise.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        "${if (exercise.type == ExerciseType.RUN_WALK) "INTERVAL" else "STRENGTH"}  |  $useCount PLAN USE${if (useCount == 1) "" else "S"}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp,
                                    )
                                }
                                IconButton(onClick = { renaming = exercise }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Rename ${exercise.name}")
                                }
                                IconButton(onClick = { deleting = exercise }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete ${exercise.name}")
                                }
                            }
                        }
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = if (state.exerciseLibrary.isEmpty()) "NO SAVED EXERCISES" else "NO MATCHES",
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
            }
        }
    }

    renaming?.let { exercise ->
        NameDialog(
            title = "Rename exercise",
            label = "Exercise name",
            initial = exercise.name,
            confirmLabel = "Save",
            onDismiss = { renaming = null },
            onSave = { name ->
                onRename(exercise.id, name)
                renaming = null
            },
        )
    }

    deleting?.let { exercise ->
        val useCount = state.plans.sumOf { plan ->
            plan.days.sumOf { day -> day.exercises.count { it.trackingId == exercise.id } }
        }
        DeleteDialog(
            title = "Delete ${exercise.name}?",
            message = buildString {
                append("This removes the exercise from the library")
                if (useCount > 0) append(" and $useCount workout day ${if (useCount == 1) "entry" else "entries"}")
                append(". Logged progress stays saved.")
                val active = state.activeWorkout
                val activeUsesExercise = active?.let { workout ->
                    state.plans.firstOrNull { it.id == workout.planId }
                        ?.days?.firstOrNull { it.id == workout.dayId }
                        ?.exercises?.any { it.trackingId == exercise.id }
                } == true
                if (activeUsesExercise) append(" The current workout will end.")
            },
            onDismiss = { deleting = null },
            onDelete = {
                onDelete(exercise.id)
                deleting = null
            },
        )
    }
}

@Composable
private fun PlanFolder(
    plan: WorkoutPlan,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (selected) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(plan.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${plan.days.size} DAYS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, contentDescription = "Rename plan") }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete plan") }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExercisePlan,
    state: AppState,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val recent = state.logs.filter { it.exerciseId == exercise.trackingId }.maxByOrNull { it.completedAt }
    val next = suggestedWeight(exercise, state.logs)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(exercise.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (exercise.type == ExerciseType.RUN_WALK) {
                            "${exercise.intervalRounds} INTERVALS  |  RUN ${formatDuration(exercise.runSeconds)}  |  WALK ${formatDuration(exercise.walkSeconds)}"
                        } else if (exercise.warmupSets.isNotEmpty() || exercise.workSets.isNotEmpty()) {
                            val workCount = exercise.workSets.size.takeIf { it > 0 } ?: exercise.targetSets
                            "$workCount WORK  |  ${exercise.warmupSets.size} WARM-UP  |  ${formatWeight(exercise.workingWeight, state.unit)}"
                        } else {
                            "${exercise.targetSets} × ${exercise.targetReps}  |  ${formatWeight(exercise.workingWeight, state.unit)}"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, contentDescription = "Edit exercise") }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete exercise") }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                if (exercise.type == ExerciseType.RUN_WALK) {
                    Text("RUN / WALK TIMER", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("REST ${formatDuration(exercise.restSeconds)}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (recent == null) "NEXT ${formatWeight(next, state.unit)}" else "LAST ${formatWeight(recent.weight, state.unit)}  |  NEXT ${formatWeight(next, state.unit)}",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPanel(title: String, message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    initial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = { Text(title) },
        text = { OutlinedTextField(value, { value = it }, label = { Text(label) }, singleLine = true) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(onClick = { onSave(value.trim()) }, enabled = value.isNotBlank()) { Text(confirmLabel) }
        },
    )
}

@Composable
private fun DeleteDialog(title: String, message: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = { Button(onClick = onDelete) { Text("Delete") } },
    )
}

@Composable
private fun ExerciseDialog(
    title: String,
    initial: ExercisePlan?,
    existingExercises: List<ExerciseDefinition>,
    unitLabel: String,
    onDismiss: () -> Unit,
    onSave: (ExercisePlan) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var sets by remember(initial) { mutableStateOf(initial?.targetSets?.toString() ?: "3") }
    var reps by remember(initial) { mutableStateOf(initial?.targetReps?.toString() ?: "5") }
    var weight by remember(initial) { mutableStateOf(initial?.workingWeight?.toString() ?: "45") }
    var increment by remember(initial) { mutableStateOf(initial?.increment?.toString() ?: "5") }
    var restSeconds by remember(initial) { mutableStateOf(initial?.restSeconds?.toString() ?: "90") }
    var exerciseType by remember(initial) { mutableStateOf(initial?.type ?: ExerciseType.STRENGTH) }
    var runSeconds by remember(initial) { mutableStateOf(initial?.runSeconds?.toString() ?: "60") }
    var walkSeconds by remember(initial) { mutableStateOf(initial?.walkSeconds?.toString() ?: "60") }
    var intervalRounds by remember(initial) { mutableStateOf(initial?.intervalRounds?.toString() ?: "5") }
    var selectedTrackingId by remember(initial) { mutableStateOf(initial?.trackingId) }
    var exerciseSearch by remember(initial) { mutableStateOf("") }
    var advancedExpanded by remember(initial) {
        mutableStateOf(initial?.let { it.warmupSets.isNotEmpty() || it.workSets.isNotEmpty() } == true)
    }
    var warmupDrafts by remember(initial) {
        mutableStateOf(initial?.warmupSets.orEmpty().map { WarmupDraft(it.reps.toString(), it.weightPercent.toString()) })
    }
    var workSetDrafts by remember(initial) {
        mutableStateOf(initial?.workSets.orEmpty().map { WorkSetDraft(it.reps.toString(), formatPlanNumber(it.weightOffset)) })
    }
    val filteredExisting = filteredExerciseLibrary(existingExercises, exerciseSearch)
    val valid = name.isNotBlank() && if (exerciseType == ExerciseType.RUN_WALK) {
        (runSeconds.toIntOrNull() ?: 0) > 0 &&
            (walkSeconds.toIntOrNull() ?: 0) > 0 &&
            (intervalRounds.toIntOrNull() ?: 0) > 0
    } else {
        (sets.toIntOrNull() ?: 0) > 0 &&
            (reps.toIntOrNull() ?: 0) > 0 &&
            weight.toDoubleOrNull() != null &&
            increment.toDoubleOrNull() != null &&
            (restSeconds.toIntOrNull() ?: 0) > 0 &&
            warmupDrafts.all { (it.reps.toIntOrNull() ?: 0) > 0 && (it.percent.toIntOrNull() ?: -1) in 0..80 } &&
            workSetDrafts.all { (it.reps.toIntOrNull() ?: 0) > 0 && it.offset.toDoubleOrNull() != null }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (initial == null && existingExercises.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("USE SAVED EXERCISE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${existingExercises.size} SAVED", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    }
                    OutlinedTextField(
                        value = exerciseSearch,
                        onValueChange = { exerciseSearch = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search exercises") },
                        singleLine = true,
                    )
                    OutlinedButton(
                        onClick = {
                            selectedTrackingId = null
                            name = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CutCornerShape(6.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("CREATE NEW EXERCISE")
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 210.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filteredExisting, key = { it.id }) { existing ->
                            val selected = selectedTrackingId == existing.id
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedTrackingId = existing.id
                                    name = existing.name
                                    sets = existing.targetSets.toString()
                                    reps = existing.targetReps.toString()
                                    weight = existing.workingWeight.toString()
                                    increment = existing.increment.toString()
                                    restSeconds = existing.restSeconds.toString()
                                    exerciseType = existing.type
                                    runSeconds = existing.runSeconds.toString()
                                    walkSeconds = existing.walkSeconds.toString()
                                    intervalRounds = existing.intervalRounds.toString()
                                    warmupDrafts = existing.warmupSets.map { WarmupDraft(it.reps.toString(), it.weightPercent.toString()) }
                                    workSetDrafts = existing.workSets.map { WorkSetDraft(it.reps.toString(), formatPlanNumber(it.weightOffset)) }
                                    advancedExpanded = warmupDrafts.isNotEmpty() || workSetDrafts.isNotEmpty()
                                },
                                shape = CutCornerShape(5.dp),
                                color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(existing.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                    Text(
                                        if (existing.type == ExerciseType.RUN_WALK) "INTERVAL" else "STRENGTH",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 9.sp,
                                    )
                                }
                            }
                        }
                        if (filteredExisting.isEmpty()) {
                            item {
                                Text(
                                    "No matching exercises",
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(name, { name = it }, label = { Text("Exercise") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseType.entries.forEach { option ->
                        ThemedChoiceChip(
                            selected = exerciseType == option,
                            onClick = { exerciseType = option },
                            label = if (option == ExerciseType.STRENGTH) "STRENGTH" else "RUN / WALK",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (exerciseType == ExerciseType.RUN_WALK) {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        NumberField(runSeconds, { runSeconds = it }, "Run seconds", Modifier.weight(1f))
                        NumberField(walkSeconds, { walkSeconds = it }, "Walk seconds", Modifier.weight(1f))
                    }
                    NumberField(intervalRounds, { intervalRounds = it }, "Number of intervals", Modifier.fillMaxWidth())
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        NumberField(sets, { sets = it }, "Sets", Modifier.weight(1f))
                        NumberField(reps, { reps = it }, "Reps", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        NumberField(weight, { weight = it }, "Start weight", Modifier.weight(1f), decimal = true)
                        NumberField(increment, { increment = it }, "Increment", Modifier.weight(1f), decimal = true)
                    }
                    NumberField(restSeconds, { restSeconds = it }, "Rest between sets (seconds)", Modifier.fillMaxWidth())
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { advancedExpanded = !advancedExpanded },
                        color = MaterialTheme.colorScheme.background,
                        shape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("ADVANCED LIFT SCHEME", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Icon(
                                if (advancedExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (advancedExpanded) "Collapse advanced lift scheme" else "Expand advanced lift scheme",
                            )
                        }
                    }
                    if (advancedExpanded) {
                        Text("WARM-UP SETS", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Start with the empty bar, then rise gradually to no more than 80% before work sets.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                        if (warmupDrafts.isEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    warmupDrafts = listOf(
                                        WarmupDraft("10", "0"),
                                        WarmupDraft("5", "60"),
                                        WarmupDraft("3", "80"),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("ADD RECOMMENDED WARM-UPS") }
                        }
                        warmupDrafts.forEachIndexed { index, draft ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("W${index + 1}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                NumberField(
                                    draft.reps,
                                    { next -> warmupDrafts = warmupDrafts.mapIndexed { itemIndex, item -> if (itemIndex == index) item.copy(reps = next) else item } },
                                    "Reps",
                                    Modifier.weight(1f),
                                )
                                NumberField(
                                    draft.percent,
                                    { next -> warmupDrafts = warmupDrafts.mapIndexed { itemIndex, item -> if (itemIndex == index) item.copy(percent = next) else item } },
                                    "% work",
                                    Modifier.weight(1f),
                                )
                                IconButton(onClick = { warmupDrafts = warmupDrafts.filterIndexed { itemIndex, _ -> itemIndex != index } }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete warm-up set ${index + 1}")
                                }
                            }
                        }
                        if (warmupDrafts.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { warmupDrafts = warmupDrafts + WarmupDraft("3", "80") },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Text("ADD WARM-UP SET")
                            }
                        }

                        Text("WORK SETS / PYRAMID", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Set reps and a positive or negative weight change from the working weight.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                        )
                        if (workSetDrafts.isEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    val count = (sets.toIntOrNull() ?: 3).coerceAtLeast(1)
                                    workSetDrafts = List(count) { WorkSetDraft(reps.ifBlank { "5" }, "0") }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("CUSTOMIZE WORK SETS") }
                        } else {
                            workSetDrafts.forEachIndexed { index, draft ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("S${index + 1}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    NumberField(
                                        draft.reps,
                                        { next -> workSetDrafts = workSetDrafts.mapIndexed { itemIndex, item -> if (itemIndex == index) item.copy(reps = next) else item } },
                                        "Reps",
                                        Modifier.weight(1f),
                                    )
                                    NumberField(
                                        draft.offset,
                                        { next -> workSetDrafts = workSetDrafts.mapIndexed { itemIndex, item -> if (itemIndex == index) item.copy(offset = next) else item } },
                                        "+/- ${unitLabel.uppercase()}",
                                        Modifier.weight(1f),
                                        decimal = true,
                                        signed = true,
                                    )
                                    IconButton(onClick = { workSetDrafts = workSetDrafts.filterIndexed { itemIndex, _ -> itemIndex != index } }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete work set ${index + 1}")
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                OutlinedButton(
                                    onClick = { workSetDrafts = workSetDrafts + WorkSetDraft(reps.ifBlank { "5" }, "0") },
                                    modifier = Modifier.weight(1f),
                                ) { Text("+ SET") }
                                TextButton(onClick = { workSetDrafts = emptyList() }, modifier = Modifier.weight(1f)) {
                                    Text("STRAIGHT SETS")
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ExercisePlan(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            trackingId = initial?.trackingId ?: selectedTrackingId ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            targetSets = sets.toInt(),
                            targetReps = reps.toInt(),
                            workingWeight = weight.toDouble(),
                            increment = increment.toDouble(),
                            restSeconds = restSeconds.toInt(),
                            type = exerciseType,
                            runSeconds = runSeconds.toInt(),
                            walkSeconds = walkSeconds.toInt(),
                            intervalRounds = intervalRounds.toInt(),
                            warmupSets = if (exerciseType == ExerciseType.STRENGTH) {
                                warmupDrafts.map { WarmupSetPlan(requireNotNull(it.reps.toIntOrNull()), requireNotNull(it.percent.toIntOrNull())) }
                            } else emptyList(),
                            workSets = if (exerciseType == ExerciseType.STRENGTH) {
                                workSetDrafts.map { WorkSetPlan(requireNotNull(it.reps.toIntOrNull()), requireNotNull(it.offset.toDoubleOrNull())) }
                            } else emptyList(),
                        ),
                    )
                },
                enabled = valid,
            ) { Text("Save") }
        },
    )
}

private fun ExercisePlan.toDefinition() = ExerciseDefinition(
    id = trackingId,
    name = name,
    targetSets = targetSets,
    targetReps = targetReps,
    workingWeight = workingWeight,
    increment = increment,
    restSeconds = restSeconds,
    type = type,
    runSeconds = runSeconds,
    walkSeconds = walkSeconds,
    intervalRounds = intervalRounds,
    warmupSets = warmupSets,
    workSets = workSets,
)

private data class WarmupDraft(val reps: String, val percent: String)

private data class WorkSetDraft(val reps: String, val offset: String)

private fun formatPlanNumber(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    decimal: Boolean = false,
    signed: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            if (
                next.isEmpty() || next == "-" && signed ||
                next.withIndex().all { (index, char) -> char.isDigit() || decimal && char == '.' || signed && char == '-' && index == 0 }
            ) onValueChange(next)
        },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
    )
}
