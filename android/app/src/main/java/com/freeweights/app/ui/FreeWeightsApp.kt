package com.freeweights.app.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeweights.app.BuildConfig
import com.freeweights.app.data.WorkoutRepository
import com.freeweights.app.model.AppState
import com.freeweights.app.model.ExerciseType
import com.freeweights.app.ui.screens.PlanScreen
import com.freeweights.app.ui.screens.ProgressScreen
import com.freeweights.app.ui.screens.ToolsScreen
import com.freeweights.app.ui.screens.WorkoutScreen
import com.freeweights.app.ui.screens.advanceRunWalkPhase
import com.freeweights.app.ui.theme.FreeWeightsTheme
import com.freeweights.app.util.completedRestTimer
import kotlinx.coroutines.delay

private data class Destination(val label: String, val icon: ImageVector)

@Composable
fun FreeWeightsApp() {
    val context = LocalContext.current
    val rootView = LocalView.current
    val repository = remember { WorkoutRepository(context.applicationContext) }
    var state by remember { mutableStateOf(repository.load()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val destinations = listOf(
        Destination("Workout", Icons.Rounded.PlayCircle),
        Destination("Plans", Icons.Rounded.Folder),
        Destination("Progress", Icons.AutoMirrored.Rounded.ShowChart),
        Destination("Tools", Icons.Rounded.Calculate),
    )

    fun updateState(next: AppState) {
        state = next
        repository.save(next)
    }

    val latestState by rememberUpdatedState(state)
    val intervalEndsAt = state.activeWorkout?.intervalEndsAt
    val restEndsAt = state.restTimer.endsAt

    LaunchedEffect(restEndsAt) {
        val end = restEndsAt ?: return@LaunchedEffect
        delay((end - System.currentTimeMillis()).coerceAtLeast(0L))
        val current = latestState
        if (current.restTimer.endsAt == end) {
            signalTimerDone(context)
            updateState(current.copy(restTimer = completedRestTimer(current.restTimer)))
        }
    }

    LaunchedEffect(intervalEndsAt, state.activeWorkout?.intervalPhase, state.activeWorkout?.currentExerciseIndex) {
        val end = intervalEndsAt ?: return@LaunchedEffect
        delay((end - System.currentTimeMillis()).coerceAtLeast(0L))
        val current = latestState
        val active = current.activeWorkout ?: return@LaunchedEffect
        if (active.intervalEndsAt != end) return@LaunchedEffect
        val plan = current.plans.firstOrNull { it.id == active.planId } ?: return@LaunchedEffect
        val day = plan.days.firstOrNull { it.id == active.dayId } ?: return@LaunchedEffect
        val exercise = day.exercises.getOrNull(active.currentExerciseIndex) ?: return@LaunchedEffect
        if (exercise.type != ExerciseType.RUN_WALK) return@LaunchedEffect
        signalTimerDone(context)
        updateState(current.copy(activeWorkout = advanceRunWalkPhase(active, exercise, end)))
    }

    DisposableEffect(context, restEndsAt, intervalEndsAt) {
        val latestEnd = listOfNotNull(restEndsAt, intervalEndsAt).maxOrNull()
        val wakeLock = latestEnd?.let { end ->
            val timeout = (end - System.currentTimeMillis()).coerceAtLeast(1_000L) + 60_000L
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${context.packageName}:activeTimer")
                .apply {
                    setReferenceCounted(false)
                    acquire(timeout)
                }
        }
        onDispose {
            if (wakeLock?.isHeld == true) wakeLock.release()
        }
    }

    DisposableEffect(rootView, state.activeWorkout != null, restEndsAt != null) {
        val previousKeepScreenOn = rootView.keepScreenOn
        rootView.keepScreenOn = state.activeWorkout != null || restEndsAt != null || previousKeepScreenOn
        onDispose { rootView.keepScreenOn = previousKeepScreenOn }
    }

    FreeWeightsTheme(
        textColor = state.themeTextColor,
        backgroundColor = state.themeBackgroundColor,
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Text(
                text = "FREE_WEIGHTS://${BuildConfig.VERSION_NAME}",
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 5.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .25.sp,
                maxLines = 1,
                softWrap = false,
            )

            when (selectedTab) {
                0 -> WorkoutScreen(state = state, onStateChange = ::updateState)
                1 -> PlanScreen(state = state, onStateChange = ::updateState)
                2 -> ProgressScreen(state = state, onStateChange = ::updateState)
                else -> ToolsScreen(state = state, onStateChange = ::updateState)
            }
        }
    }
    }
}
