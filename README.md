# ZenBreath - Breathing Tracking App

A comprehensive Android breathing exercise tracking application built with Jetpack Compose and
modern Android development practices.

## Features

### Core Functionality

1. **Countdown Timer** - Customizable breathing exercise timer with visual progress ring
2. **Heart Rate Monitoring** - Track heart rate at start (SHR) and end (EHR) of each session
3. **Rep Counter** - Track multiple breathing exercise repetitions
4. **Session History** - View past sessions in descending order
5. **Delete Sessions** - Long-press any session to delete with confirmation
6. **CSV Export** - Export all session data for analysis

### UI Component Reference

To help identify and refer to different parts of the ZenBreath screen, please use the following labels:

- **Session Tracking Card**: The top-most area containing the "Start" and "End" workout buttons, as well as the Start (S:), Duration (Dur:), and End (E:) timestamps.
- **Breathing Hub**: The central interactive area featuring the large circular progress ring, the current timer clock (e.g., "60s"), and the current "Target" value.
- **Rep Lifecycle**: The row immediately below the timer that displays your current rep count (e.g., "Rep Count: 1/10") and the reset icon.
- **Health Metrics Card**: The area displaying your real-time heart rate (BPM) along with the recorded minimum ("Min:") and maximum ("Max:") BPM for the active session.
- **Exercise Configuration**: The settings row near the bottom used to adjust the **Timer** duration, **Reps** count, and **Target** goal using increment/decrement arrows.
- **Overall History**: The scrollable list at the very bottom showcasing all previous breathing sessions.

## Technical Architecture

### Data Layer

- **Room Database** - Local SQLite database for persistent storage
- **BreathingSession Entity** - Stores all session data including timestamps and heart rates
- **Repository Pattern** - Clean separation of data access logic

### Business Logic

- **ViewModel** - Manages UI state and business logic
- **Heart Rate Service** - Interfaces with Health Services API for heart rate monitoring
- **Timer Management** - Countdown timer with automatic stop functionality

### UI Layer

- **Jetpack Compose** - Modern declarative UI framework
- **Material Design 3** - Latest Material Design components
- **Custom Components**:
    - `TimerDisplay` - Circular progress ring with countdown
    - `SessionItem` - Individual session history card
    - Dialogs for timer and rep configuration

## Data Export

The app supports CSV export with the following format:

```csv
ID,Start Time,End Time,Start Heart Rate,End Heart Rate,Rep Number,Total Reps,Duration (seconds)
1,2025-01-06 14:30:00,2025-01-06 14:31:00,72,68,1,10,60
```

CSV files are saved to the app's external files directory and can be:

- Shared via Android's share functionality
- Opened in Excel or Google Sheets for analysis
- Used for long-term health tracking

## Permissions

The app requires the following permissions:

- `BODY_SENSORS` - For heart rate monitoring
- `ACTIVITY_RECOGNITION` - For health services integration
- `WRITE_EXTERNAL_STORAGE` (API < 33) - For CSV export
- `READ_EXTERNAL_STORAGE` (API < 33) - For file access

## Usage Instructions

1. **Start a Session**
    - Set desired timer duration by tapping the edit icon
    - Set desired rep count by tapping the edit icon
    - Tap START button to begin
    - Heart rate is automatically captured at start

2. **During Session**
    - Timer counts down automatically
    - Heart rate is monitored continuously
    - STOP button turns red during active session

3. **End Session**
    - Tap STOP to end manually, or
    - Wait for timer to reach zero (auto-stops)
    - Session data is automatically saved to database
    - Rep counter increments

4. **View History**
    - Scroll down to see past sessions
    - Sessions displayed in descending order (newest first)
    - Each entry shows timestamps and heart rates

5. **Export Data**
    - Tap the share button (floating action button)
    - CSV file is generated and path is displayed
    - File can be found in app's external storage

6. **Reset Reps**
    - Tap refresh icon next to rep counter
    - Resets current rep count to 0

## Technical Requirements

- **Minimum SDK**: 35 (Android 15)
- **Target SDK**: 36
- **Kotlin**: 2.0.21
- **Compose BOM**: 2024.09.00
- **Room**: 2.6.1
- **Health Services**: 1.1.0-alpha03

## Dependencies

### Core

- AndroidX Core KTX
- AndroidX Lifecycle Runtime KTX
- AndroidX Activity Compose

### UI

- Jetpack Compose UI
- Material Design 3
- Compose ViewModel

### Data

- Room Database (Runtime, KTX, Compiler)
- Kotlinx Coroutines

### Health

- Health Services Client
- Guava (ListenableFuture support)
- Kotlinx Coroutines Guava

## Building the App

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Project Structure

```
app/src/main/java/com/example/zenbreath/
├── data/
│   ├── BreathingSession.kt      # Entity model
│   ├── BreathingSessionDao.kt   # Data access object
│   ├── AppDatabase.kt           # Room database
│   └── BreathingRepository.kt   # Repository pattern
├── service/
│   └── HeartRateService.kt      # Heart rate monitoring
├── viewmodel/
│   └── BreathingViewModel.kt    # UI state management
├── ui/
│   ├── components/
│   │   ├── TimerDisplay.kt      # Circular timer
│   │   └── SessionItem.kt       # History item
│   ├── screens/
│   │   └── HomeScreen.kt        # Main screen
│   └── theme/                   # App theming
└── MainActivity.kt              # Entry point
```

## Future Enhancements

Potential features for future versions:

- Real-time heart rate monitoring during exercise
    - *Merge Recommendation: Define a clean HeartRateSource interface so the host app can 'plug in' its own HR stream (e.g., from Health Connect or specialized sensors) without modifying the ZenBreath core timer.*
- Breathing patterns (inhale/exhale guidance)
    - *Merge Recommendation: Abstract the breathing logic into a 'BreathingEngine' component that takes pattern configurations as input, making it easy to share patterns with other health features in the host app.*
    - *Implementation Recommendation (Developer View): Use an 'Organic UI' approach where the central Breathing Hub dynamically scales (1.0x to 1.15x) using a `spring` animation to mimic lung expansion. Overlay a minimalist state text (Inhale/Hold/Exhale) and synchronize gentle `VibrationEffect` pulses with phase transitions. Architect this as a `StateFlow`-driven engine where each pattern is a simple list of `Phase(type, durationMillis)`—this keeps the logic decoupled from the UI and perfectly ready for the host app's data layer.*
- Statistics and analytics dashboard
    - *Merge Recommendation: Use a common 'SessionRecord' data model that aligns with the host app's existing health schema to avoid data migration headaches later.*
- Wear OS companion app
    - *Merge Recommendation: Finalize the Wearable DataClient sync logic as a standalone module so it can handle multi-app data synchronization if the host app also has a watch component.*
- Cloud sync and backup
    - *Merge Recommendation: Focus on making the local Room database exportable (JSON/GeoJSON) rather than building a custom sync engine, allowing the host app's existing backend to consume the data.*
- Custom breathing patterns
    - *Merge Recommendation: Store patterns as serialized JSON in stable storage, facilitating easy import/export during the merge.*
- Guided breathing exercises
    - *Merge Recommendation: Treat 'Guided' content as an external asset layer that can be swapped or expanded by the host app's content delivery system.*
    - *Implementation Recommendation (Developer View): Transition from a simple UI timer to a robust media-driven architecture using `Jetpack Media3`. Wrap guided sessions in a `MediaSession` to support background playback and lock-screen controls. Define a `GuidedSession` schema (Audio URL + Script JSON) so the host app can dynamically inject any narration program. Synchronize the existing 'Breathing Hub' animations and haptics with `Media3` playback position via a custom `Player.Listener` to ensure the visuals and narration are always in perfect lock-step.*
- Progress tracking and goals
    - *Merge Recommendation: Leverage standard health-tech 'Achievement' models so you can consolidate user goals across both apps.*
- Notifications and reminders
    - *Merge Recommendation: Use a centralized 'ReminderManager' that doesn't conflict with the host app's notification channels; ensure ZenBreath reminders are categorized and toggleable.*
- Dark mode support
    - *Merge Recommendation: Use standard Material 3 Tonal Palette naming conventions to prevent 'Color Resource' collisions when multiple themes are merged into a single app project.*

## License

This project is created for personal use and learning purposes.

## Contact

For questions or suggestions about this app, please open an issue in the repository.
