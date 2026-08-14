package com.freeweights.app.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.freeweights.app.model.WeightUnit
import com.freeweights.app.util.PlateLoad
import kotlinx.coroutines.delay

@Composable
fun RestTimerPanel(initialDuration: Int = 90, title: String = "REST TIMER") {
    val context = LocalContext.current
    var duration by rememberSaveable(initialDuration) { mutableIntStateOf(initialDuration.coerceAtLeast(1)) }
    var remaining by rememberSaveable(initialDuration) { mutableIntStateOf(initialDuration.coerceAtLeast(1)) }
    var running by rememberSaveable(initialDuration) { mutableStateOf(false) }
    var customText by rememberSaveable(initialDuration) { mutableStateOf(initialDuration.toString()) }
    val progress = remaining.toFloat() / duration.coerceAtLeast(1)

    LaunchedEffect(running, remaining) {
        if (!running) return@LaunchedEffect
        if (remaining > 0) {
            delay(1_000)
            remaining -= 1
        } else {
            running = false
            signalTimerDone(context)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 20.dp, bottomStart = 20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                IconButton(onClick = { remaining = duration; running = false }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Reset timer")
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(170.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp,
                    gapSize = 2.dp,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatDuration(remaining), fontSize = 38.sp, fontWeight = FontWeight.Black)
                    Text(if (running) "COUNTING" else "STANDBY", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.height(13.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                listOf(60, 90, 120, 180).forEach { seconds ->
                    FilterChip(
                        selected = duration == seconds,
                        onClick = {
                            duration = seconds
                            remaining = seconds
                            customText = seconds.toString()
                            running = false
                        },
                        label = { Text(formatDuration(seconds)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = customText,
                onValueChange = { next ->
                    if (next.length <= 6 && next.all { it.isDigit() || it == ':' }) {
                        customText = next
                        parseRestTime(next)?.takeIf { it > 0 }?.let { seconds ->
                            duration = seconds
                            remaining = seconds
                            running = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("CUSTOM TIME (SECONDS OR M:SS)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (remaining == 0) remaining = duration
                    running = !running
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(8.dp),
            ) {
                Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                Text(if (running) "PAUSE" else "START REST", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun parseRestTime(value: String): Int? {
    val clean = value.trim()
    if (clean.isEmpty()) return null
    if (':' !in clean) return clean.toIntOrNull()
    val parts = clean.split(':')
    if (parts.size != 2) return null
    val minutes = parts[0].toIntOrNull() ?: return null
    val seconds = parts[1].toIntOrNull() ?: return null
    if (seconds !in 0..59) return null
    return minutes * 60 + seconds
}

@Composable
fun PlateLoadPanel(load: PlateLoad, unit: WeightUnit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shape = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PLATES PER SIDE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            PlateStack(load, unit)
            Spacer(Modifier.height(12.dp))
            Text(
                if (load.isExact) "${formatWeight(load.loadedWeight, unit)} TOTAL"
                else "${formatWeight(load.loadedWeight, unit)} LOADED  |  ${formatWeight(load.remainder, unit)} SHORT",
                color = if (load.isExact) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun PlateStack(load: PlateLoad, unit: WeightUnit) {
    if (load.platesPerSide.isEmpty()) {
        Text("BAR ONLY", fontSize = 19.sp, fontWeight = FontWeight.Black)
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        load.platesPerSide.forEachIndexed { index, plate ->
            val plateColor = olympicPlateColor(plate, unit)
            val fill = plateColor.fill()
            Surface(
                modifier = Modifier.height((55 - index.coerceAtMost(4) * 4).dp),
                shape = CutCornerShape(5.dp),
                color = fill,
                border = BorderStroke(1.dp, if (plateColor == OlympicPlateColor.BLACK) Color(0xFF555555) else fill),
            ) {
                Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    Text(formatPlate(plate), color = plateColor.label(), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

internal enum class OlympicPlateColor {
    RED,
    BLUE,
    YELLOW,
    GREEN,
    BLACK,
    WHITE,
    GRAY,
}

internal fun olympicPlateColor(weight: Double, unit: WeightUnit): OlympicPlateColor = when (unit) {
    WeightUnit.LB -> when (weight) {
        55.0 -> OlympicPlateColor.RED
        45.0 -> OlympicPlateColor.BLUE
        35.0 -> OlympicPlateColor.YELLOW
        25.0 -> OlympicPlateColor.GREEN
        15.0 -> OlympicPlateColor.BLACK
        10.0 -> OlympicPlateColor.GRAY
        else -> OlympicPlateColor.WHITE
    }
    WeightUnit.KG -> when (weight) {
        25.0, 2.5 -> OlympicPlateColor.RED
        20.0 -> OlympicPlateColor.BLUE
        15.0 -> OlympicPlateColor.YELLOW
        10.0, 1.25 -> OlympicPlateColor.GREEN
        5.0 -> OlympicPlateColor.WHITE
        else -> OlympicPlateColor.GRAY
    }
}

private fun OlympicPlateColor.fill(): Color = when (this) {
    OlympicPlateColor.RED -> Color(0xFFD71920)
    OlympicPlateColor.BLUE -> Color(0xFF1769D2)
    OlympicPlateColor.YELLOW -> Color(0xFFF2C230)
    OlympicPlateColor.GREEN -> Color(0xFF12B84B)
    OlympicPlateColor.BLACK -> Color(0xFF1D1D1D)
    OlympicPlateColor.WHITE -> Color(0xFFF1F1ED)
    OlympicPlateColor.GRAY -> Color(0xFFADB3B5)
}

private fun OlympicPlateColor.label(): Color = when (this) {
    OlympicPlateColor.RED, OlympicPlateColor.BLUE, OlympicPlateColor.BLACK -> Color.White
    else -> Color.Black
}

fun formatPlate(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

private fun signalTimerDone(context: Context) {
    runCatching {
        ToneGenerator(AudioManager.STREAM_ALARM, 85).apply {
            startTone(ToneGenerator.TONE_PROP_BEEP2, 700)
        }
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
