# Free Weights

Free Weights is a native Android strength-training companion built with Kotlin and Jetpack Compose.

Current build: 2.12.0. The version is shown in the app header for installation verification.

## Included

- Empty-by-default plan library with selectable, editable plan folders
- Renameable and deletable workout days with stable day selection while editing
- Exercise targets for sets, reps, working weight, progression increments, and rest periods
- Collapsible advanced lift schemes with recommended bar-to-80% warm-ups and per-set pyramid reps and weight offsets
- Current set weights resolved from the saved bar and available plate settings
- Warm-up sets excluded from logged training volume and progression completion
- Start Workout flow with set tracking, manual rest control, and current-set plate loading
- Active workouts persist across tabs and app restarts
- Run/walk interval exercises with configurable run time, walk time, interval count, audible phase alerts, skip, previous, and an in-workout interval timer
- Stacked run/walk progress bars with total session duration and per-interval timing breakdowns
- Per-set complete, failed, and undo controls
- Searchable, alphabetized shared exercise library for continuous lift progression across plans and splits
- Exercise manager with rename propagation and deletion from all plan-day uses while preserving logged progress
- Inline collapsible lift manager on the Plans screen
- Inline collapsible plan selector with the active plan shown in its header
- Compact single-line phone layouts for headers, workout summaries, and session metadata
- Theme-matched progress metrics and day selectors
- Automatic next-day selection based on each plan's last completed day
- Completed-session logging grouped into expandable session folders
- Editable historical session sets, reps, weight, and failed-set counts
- Confirmed deletion of complete historical workout sessions
- JSON export and confirmed import for plans, exercises, progress, settings, and active workout state
- Separate plan-only JSON export and merge import that preserves progress and settings
- Integrated 128 × 128 borderless black and neon-green hacker-themed launcher icon
- Automatic next-weight recommendations
- Progress dashboard with personal bests, total volume, per-session volume, recent session folders, touch-selectable chart points, and exercise trends
- Rest timer with presets, typed custom times, pause/reset controls, vibration, and sound
- Plate calculator for pounds and kilograms with editable bar weight and available plate inventory
- Olympic competition color coding for pound and kilogram plate loads
- Collapsible Tools sections ordered with Theme and Data Management at the bottom
- Separate confirmed controls to delete all progress, all plans, or all exercises
- Custom text and background theme colors with hacker-style presets, hex entry, and `#00FF66` default text
- Theme-matched Add Exercise dialog background
- Theme-matched Strength and Run / Walk selectors in Add Exercise
- Exact 128 × 128 PNG launcher resources packaged in the APK
- Unit tests for empty defaults, rest-time parsing, plate inventories, plate loading, and progression logic

All personal workout data stays on the device in the app-private `free_weights_v2` SharedPreferences file. No account or network connection is required.

## Open and run

1. Open this folder in Android Studio.
2. Allow Gradle sync to install the declared Android SDK and dependencies.
3. Run the `app` configuration on an Android 8.0 or newer device or emulator.

The project targets Android API 36, uses JDK 17, Android Gradle Plugin 8.13, and Gradle 8.13.
