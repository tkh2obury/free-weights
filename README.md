# Free Weights

[![Cross-platform CI](https://github.com/tkh2obury/free-weights/actions/workflows/ci.yml/badge.svg)](https://github.com/tkh2obury/free-weights/actions/workflows/ci.yml)

Free Weights is an open-source, local-first workout tracker for Android and iOS. It uses a compact terminal-inspired interface with `#00FF66` as the default text color.

## Features

- Collapsible multi-plan and day management
- Rename and delete plans, days, exercises, and workout sessions
- Strength and run/walk exercise tracking
- Advanced warm-up sets and custom ascending or descending pyramid schemes
- Bar-aware plate loading based on the available equipment
- Olympic plate colors for 10, 15, 25, 35, 45, and 55 lb plates and their metric equivalents
- Run/walk interval timer with total and segment-duration charts
- Exercise progress and session volume history
- Separate deletion controls for progress, plans, and exercises
- JSON backup export and restore
- Theme colors stored locally, with `#00FF66` as the default text color
- No account, ads, analytics, or network access

## Repository layout

- `android/`: Kotlin, Jetpack Compose, Room, and Material 3 app
- `ios/`: native SwiftUI app plus platform-independent workout logic and tests

## Android

Requirements: Android Studio Ladybug or newer and JDK 17.

```bash
cd android
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`.

## iOS

Requirements: macOS, Xcode 16 or newer, and iOS 17 or newer.

1. Open `ios/FreeWeightsIOS.xcodeproj` in Xcode.
2. Select the `FreeWeightsIOS` scheme and an iPhone simulator.
3. Press Run.

The app uses the bundle identifier `com.freeweights.ios`. Change it and select your Apple Development team before installing on a physical device.

Run the platform-independent tests with:

```bash
cd ios
swift test
```

Build the simulator app from the command line with:

```bash
xcodebuild -project FreeWeightsIOS.xcodeproj \
  -scheme FreeWeightsIOS \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO build
```

## Data and privacy

Workout data remains on the device. Backup files are JSON documents selected explicitly by the user. Deleting progress, plans, or exercises is independent and requires confirmation.

## Contributing

Issues and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT. See [LICENSE](LICENSE).
