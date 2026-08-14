package com.freeweights.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeweights.app.data.WorkoutRepository
import com.freeweights.app.data.PlanImport
import com.freeweights.app.data.mergePlanImport
import com.freeweights.app.model.AppState
import com.freeweights.app.model.WeightUnit
import com.freeweights.app.ui.PlateLoadPanel
import com.freeweights.app.ui.RestTimerPanel
import com.freeweights.app.ui.SectionHeader
import com.freeweights.app.ui.formatPlate
import com.freeweights.app.ui.theme.normalizeThemeHex
import com.freeweights.app.ui.theme.themeColor
import com.freeweights.app.util.calculatePlates
import com.freeweights.app.util.deleteAllExercises
import com.freeweights.app.util.deleteAllPlans
import com.freeweights.app.util.deleteAllProgress

@Composable
fun ToolsScreen(state: AppState, onStateChange: (AppState) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SectionHeader("Training tools") }
        item {
            CollapsibleTool("REST TIMER", initiallyExpanded = true) {
                RestTimerPanel()
            }
        }
        item {
            CollapsibleTool("PLATE CALCULATOR") {
                PlateCalculatorCard(state, onStateChange)
            }
        }
        item {
            CollapsibleTool("THEME") {
                ThemeSettingsCard(state, onStateChange)
            }
        }
        item {
            CollapsibleTool("DATA MANAGEMENT") {
                DataManagementCard(state, onStateChange)
            }
        }
    }
}

@Composable
private fun CollapsibleTool(
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            color = MaterialTheme.colorScheme.surface,
            shape = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 13.sp)
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse $title" else "Expand $title",
                )
            }
        }
        if (expanded) content()
    }
}

@Composable
private fun ThemeSettingsCard(state: AppState, onStateChange: (AppState) -> Unit) {
    var textHex by remember(state.themeTextColor) { mutableStateOf(state.themeTextColor) }
    var backgroundHex by remember(state.themeBackgroundColor) { mutableStateOf(state.themeBackgroundColor) }
    val normalizedText = normalizeThemeHex(textHex)
    val normalizedBackground = normalizeThemeHex(backgroundHex)
    val textPresets = listOf("#B7FFD0", "#00FF66", "#00E5FF", "#FFC857", "#FFFFFF", "#FF55FF")
    val backgroundPresets = listOf("#020704", "#000000", "#07111F", "#16051D", "#1A1203", "#F4F4F4")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("THEME", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text("Choose the interface text and background colors.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            OutlinedTextField(
                value = textHex,
                onValueChange = { textHex = it.take(7) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("TEXT COLOR") },
                supportingText = { Text("Hex format: #RRGGBB") },
                singleLine = true,
            )
            ColorPresetRow(textPresets) { textHex = it }
            OutlinedTextField(
                value = backgroundHex,
                onValueChange = { backgroundHex = it.take(7) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("BACKGROUND COLOR") },
                supportingText = { Text("Hex format: #RRGGBB") },
                singleLine = true,
            )
            ColorPresetRow(backgroundPresets) { backgroundHex = it }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = themeColor(backgroundHex, MaterialTheme.colorScheme.background),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = CutCornerShape(7.dp),
            ) {
                Text(
                    "THEME PREVIEW // 0123456789",
                    modifier = Modifier.padding(13.dp),
                    color = themeColor(textHex, MaterialTheme.colorScheme.onBackground),
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                onClick = {
                    onStateChange(
                        state.copy(
                            themeTextColor = requireNotNull(normalizedText),
                            themeBackgroundColor = requireNotNull(normalizedBackground),
                        ),
                    )
                },
                enabled = normalizedText != null && normalizedBackground != null,
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(8.dp),
            ) { Text("APPLY THEME") }
        }
    }
}

@Composable
private fun ColorPresetRow(colors: List<String>, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.forEach { hex ->
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(themeColor(hex, Color.Black), CutCornerShape(5.dp))
                    .clickable { onSelect(hex) },
            )
        }
    }
}

@Composable
private fun DataManagementCard(state: AppState, onStateChange: (AppState) -> Unit) {
    val context = LocalContext.current
    val repository = remember { WorkoutRepository(context.applicationContext) }
    var pendingImport by remember { mutableStateOf<AppState?>(null) }
    var pendingPlanImport by remember { mutableStateOf<PlanImport?>(null) }
    var pendingDeletion by remember { mutableStateOf<DataDeletion?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            status = runCatching {
                val output = context.contentResolver.openOutputStream(uri) ?: error("Unable to open export file")
                output.bufferedWriter().use { it.write(repository.exportJson(state)) }
                "BACKUP EXPORTED"
            }.getOrElse { "EXPORT FAILED: ${it.message ?: "UNKNOWN ERROR"}" }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val input = context.contentResolver.openInputStream(uri) ?: error("Unable to open backup")
                val raw = input.bufferedReader().use { it.readText() }
                repository.importJson(raw)
            }.onSuccess {
                pendingImport = it
                status = null
            }.onFailure {
                status = "IMPORT FAILED: ${it.message ?: "INVALID BACKUP"}"
            }
        }
    }

    val exportPlansLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            status = runCatching {
                val output = context.contentResolver.openOutputStream(uri) ?: error("Unable to open export file")
                output.bufferedWriter().use { it.write(repository.exportPlansJson(state)) }
                "PLANS EXPORTED"
            }.getOrElse { "EXPORT FAILED: ${it.message ?: "UNKNOWN ERROR"}" }
        }
    }

    val importPlansLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val input = context.contentResolver.openInputStream(uri) ?: error("Unable to open plan file")
                val raw = input.bufferedReader().use { it.readText() }
                repository.importPlansJson(raw)
            }.onSuccess {
                pendingPlanImport = it
                status = null
            }.onFailure {
                status = "PLAN IMPORT FAILED: ${it.message ?: "INVALID FILE"}"
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("DATA MANAGEMENT", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                "Back up, restore, or permanently remove one category of workout data.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
            Button(
                onClick = { exportLauncher.launch("free-weights-backup.json") },
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(8.dp),
            ) {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
                Text("EXPORT ALL DATA")
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(8.dp),
            ) {
                Icon(Icons.Rounded.FileUpload, contentDescription = null)
                Text("IMPORT BACKUP")
            }
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(
                    onClick = { exportPlansLauncher.launch("free-weights-plans.json") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(8.dp),
                ) {
                    Icon(Icons.Rounded.FileDownload, contentDescription = null)
                    Text("EXPORT PLANS")
                }
                OutlinedButton(
                    onClick = { importPlansLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CutCornerShape(8.dp),
                ) {
                    Icon(Icons.Rounded.FileUpload, contentDescription = null)
                    Text("IMPORT PLANS")
                }
            }
            Text("DELETE DATA", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Black)
            DataDeleteButton("DELETE ALL PROGRESS", state.logs.isNotEmpty()) { pendingDeletion = DataDeletion.PROGRESS }
            DataDeleteButton("DELETE ALL PLANS", state.plans.isNotEmpty()) { pendingDeletion = DataDeletion.PLANS }
            DataDeleteButton("DELETE ALL EXERCISES", state.exerciseLibrary.isNotEmpty()) { pendingDeletion = DataDeletion.EXERCISES }
            status?.let {
                Text(
                    it,
                    color = if (it.startsWith("IMPORT FAILED") || it.startsWith("EXPORT FAILED")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    pendingImport?.let { imported ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Import backup?") },
            text = {
                Text(
                    "Replace current data with ${imported.plans.size} plans, ${imported.exerciseLibrary.size} exercises, and ${imported.logs.size} progress entries?",
                )
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } },
            confirmButton = {
                Button(
                    onClick = {
                        onStateChange(imported)
                        pendingImport = null
                        status = "BACKUP IMPORTED"
                    },
                ) { Text("Import") }
            },
        )
    }

    pendingPlanImport?.let { imported ->
        AlertDialog(
            onDismissRequest = { pendingPlanImport = null },
            title = { Text("Import plans?") },
            text = {
                Text(
                    "Merge ${imported.plans.size} plans and ${imported.exerciseLibrary.size} exercise definitions? Progress, settings, and the active workout remain unchanged.",
                )
            },
            dismissButton = { TextButton(onClick = { pendingPlanImport = null }) { Text("Cancel") } },
            confirmButton = {
                Button(
                    onClick = {
                        onStateChange(mergePlanImport(state, imported))
                        pendingPlanImport = null
                        status = "PLANS IMPORTED"
                    },
                ) { Text("Merge") }
            },
        )
    }

    pendingDeletion?.let { deletion ->
        val details = when (deletion) {
            DataDeletion.PROGRESS -> "Delete all ${state.logs.size} progress entries? Plans and exercises will remain."
            DataDeletion.PLANS -> "Delete all ${state.plans.size} plans? Exercises and progress will remain. Any active workout will end."
            DataDeletion.EXERCISES -> "Delete all ${state.exerciseLibrary.size} exercises and remove them from every plan? Progress will remain. Any active workout will end."
        }
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("${deletion.label}?") },
            text = { Text("$details This cannot be undone.") },
            dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Cancel") } },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = when (deletion) {
                            DataDeletion.PROGRESS -> deleteAllProgress(state)
                            DataDeletion.PLANS -> deleteAllPlans(state)
                            DataDeletion.EXERCISES -> deleteAllExercises(state)
                        }
                        onStateChange(updated)
                        status = "${deletion.label.uppercase()} COMPLETE"
                        pendingDeletion = null
                    },
                ) { Text("Delete") }
            },
        )
    }
}

private enum class DataDeletion(val label: String) {
    PROGRESS("Delete all progress"),
    PLANS("Delete all plans"),
    EXERCISES("Delete all exercises"),
}

@Composable
private fun DataDeleteButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(8.dp),
    ) {
        Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
        Text(label)
    }
}

@Composable
private fun PlateCalculatorCard(state: AppState, onStateChange: (AppState) -> Unit) {
    val unit = state.unit
    var targetText by rememberSaveable(unit) { mutableStateOf(if (unit == WeightUnit.LB) "225" else "100") }
    var barText by rememberSaveable(unit, state.barWeight(unit)) { mutableStateOf(formatPlate(state.barWeight(unit))) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    val available = state.availablePlates(unit)
    val target = targetText.toDoubleOrNull() ?: 0.0
    val bar = barText.toDoubleOrNull() ?: state.barWeight(unit)
    val load = calculatePlates(target, bar, available)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("PLATE CALCULATOR", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    WeightUnit.entries.forEach { option ->
                        FilterChip(
                            selected = option == unit,
                            onClick = { onStateChange(state.copy(unit = option)) },
                            label = { Text(option.label.uppercase()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            ),
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                WeightField(targetText, { targetText = it }, "TARGET ${unit.label.uppercase()}", Modifier.weight(1f))
                WeightField(barText, { barText = it }, "BAR ${unit.label.uppercase()}", Modifier.weight(1f))
            }

            PlateLoadPanel(load, unit)

            Text(
                "AVAILABLE: ${available.joinToString("  ") { formatPlate(it) }} ${unit.label}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
            )
            OutlinedButton(
                onClick = { showInventoryDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(7.dp),
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null)
                Text("EDIT AVAILABLE PLATES")
            }
        }
    }

    if (showInventoryDialog) {
        PlateInventoryDialog(
            unit = unit,
            plates = available,
            barWeight = state.barWeight(unit),
            onDismiss = { showInventoryDialog = false },
            onSave = { plates, barWeight ->
                val next = if (unit == WeightUnit.LB) {
                    state.copy(availableLbPlates = plates, lbBarWeight = barWeight)
                } else {
                    state.copy(availableKgPlates = plates, kgBarWeight = barWeight)
                }
                onStateChange(next)
                barText = formatPlate(barWeight)
                showInventoryDialog = false
            },
        )
    }
}

@Composable
private fun PlateInventoryDialog(
    unit: WeightUnit,
    plates: List<Double>,
    barWeight: Double,
    onDismiss: () -> Unit,
    onSave: (List<Double>, Double) -> Unit,
) {
    var plateText by remember(plates) { mutableStateOf(plates.joinToString(", ") { formatPlate(it) }) }
    var barText by remember(barWeight) { mutableStateOf(formatPlate(barWeight)) }
    val parsed = parsePlateInventory(plateText)
    val parsedBar = barText.toDoubleOrNull()
    val valid = parsed.isNotEmpty() && parsedBar != null && parsedBar > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Available ${unit.label.uppercase()} plates") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("Enter one plate from each available pair.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                OutlinedTextField(
                    value = plateText,
                    onValueChange = { plateText = it },
                    label = { Text("PLATES, COMMA SEPARATED") },
                    supportingText = { Text("Example: 45, 25, 10, 5, 2.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                WeightField(barText, { barText = it }, "DEFAULT BAR ${unit.label.uppercase()}", Modifier.fillMaxWidth())
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(onClick = { onSave(parsed, requireNotNull(parsedBar)) }, enabled = valid) { Text("Save") }
        },
    )
}

fun parsePlateInventory(value: String): List<Double> = value
    .split(',', ';', ' ', '\n')
    .mapNotNull { it.trim().takeIf(String::isNotEmpty)?.toDoubleOrNull() }
    .filter { it > 0 }
    .distinct()
    .sortedDescending()

@Composable
private fun WeightField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { next -> if (next.isEmpty() || next.all { it.isDigit() || it == '.' }) onValueChange(next) },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
