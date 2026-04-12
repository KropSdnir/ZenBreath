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

### Screen Layout

The home screen displays:

- Current date (MM/DD/YYYY format)
- Circular timer display with countdown
- Configurable timer duration (30s, 1min, 2min, 3min, 5min)
- Configurable repetition count (5, 10, 15, 20, 25, 30)
- START/STOP button
- Rep counter (e.g., "Rep Count: 1/10")
- Session history list showing:
    - S: Start timestamp (HH:MM)
    - E: End timestamp (HH:MM)
    - SHR: Start heart rate (BPM)
    - EHR: End heart rate (BPM)

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
- Breathing patterns (inhale/exhale guidance)
- Statistics and analytics dashboard
- Wear OS companion app
- Cloud sync and backup
- Custom breathing patterns
- Guided breathing exercises
- Progress tracking and goals
- Notifications and reminders
- Dark mode support

## License

This project is created for personal use and learning purposes.

## Contact

For questions or suggestions about this app, please open an issue in the repository.
