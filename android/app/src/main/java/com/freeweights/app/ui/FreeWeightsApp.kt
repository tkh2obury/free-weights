package com.freeweights.app.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freeweights.app.BuildConfig
import com.freeweights.app.data.WorkoutRepository
import com.freeweights.app.model.AppState
import com.freeweights.app.ui.screens.PlanScreen
import com.freeweights.app.ui.screens.ProgressScreen
import com.freeweights.app.ui.screens.ToolsScreen
import com.freeweights.app.ui.screens.WorkoutScreen
import com.freeweights.app.ui.theme.FreeWeightsTheme

private data class Destination(val label: String, val icon: ImageVector)

@Composable
fun FreeWeightsApp() {
    val context = LocalContext.current
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
