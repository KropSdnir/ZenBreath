# ZenBreath - Quick Start Guide

## For Users

### First Time Setup

1. Install the app on your Android device (requires Android 15+)
2. On first launch, grant permissions:
    - Body Sensors (for heart rate monitoring)
    - Activity Recognition (for health features)
3. App is ready to use!

### Using the App

#### Starting Your First Session

1. Open ZenBreath
2. Default settings are: 60 seconds timer, 10 reps
3. Tap the large **START** button (purple)
4. Watch the timer count down
5. Tap **STOP** (red) to end, or wait for auto-stop

#### Customizing Your Session

- **Change Timer**: Tap edit icon next to "Timer: 60s"
    - Choose: 30s, 1min, 2min, 3min, or 5min
- **Change Reps**: Tap edit icon next to "Rep: 10"
    - Choose: 5, 10, 15, 20, 25, or 30 reps

#### Tracking Your Progress

- View session history below the controls
- Each entry shows:
    - Start time (S:)
    - End time (E:)
    - Start heart rate (SHR:)
    - End heart rate (EHR:)
- Long-press any session to delete it
- Confirmation dialog prevents accidental deletion

#### Exporting Your Data

1. Tap the share button (bottom-right corner)
2. File saved to your device
3. Open with Excel, Sheets, or any CSV viewer
4. Analyze your breathing patterns over time

## For Developers

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd ZenBreath

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTest
```

### Project Requirements

- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK 35+
- Kotlin 2.0.21+

### Key Files to Know

**Data Layer:**

- `data/BreathingSession.kt` - Database entity
- `data/BreathingSessionDao.kt` - Database queries
- `data/AppDatabase.kt` - Room database setup
- `data/BreathingRepository.kt` - Data operations & CSV export

**Business Logic:**

- `viewmodel/BreathingViewModel.kt` - State management & business logic
- `service/HeartRateService.kt` - Heart rate monitoring

**UI Layer:**

- `ui/screens/HomeScreen.kt` - Main application screen
- `ui/components/TimerDisplay.kt` - Circular timer with progress
- `ui/components/SessionItem.kt` - Session history items

**Entry Point:**

- `MainActivity.kt` - App entry point & permissions

### Architecture Pattern

The app follows **MVVM (Model-View-ViewModel)** architecture:

- **Model**: Room database entities and repository
- **View**: Jetpack Compose UI components
- **ViewModel**: State management with StateFlow

### Database Schema

```sql
CREATE TABLE breathing_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    startTimestamp INTEGER NOT NULL,
    endTimestamp INTEGER NOT NULL,
    startHeartRate INTEGER NOT NULL,
    endHeartRate INTEGER NOT NULL,
    repNumber INTEGER NOT NULL,
    totalReps INTEGER NOT NULL,
    timerDuration INTEGER NOT NULL
);
```

### Adding New Features

**To add a new timer duration:**

1. Edit `HomeScreen.kt`
2. Find `TimerDurationDialog`
3. Add new duration to `durations` list

**To add a new rep count:**

1. Edit `HomeScreen.kt`
2. Find `RepCountDialog`
3. Add new count to `repCounts` list

**To modify CSV export format:**

1. Edit `BreathingRepository.kt`
2. Find `formatSessionsToCSV` function
3. Modify header and data row formatting

### Testing Heart Rate Integration

Currently using simulated heart rate (60-100 BPM). To integrate real sensor:

1. Edit `HeartRateService.kt`
2. Implement `MeasureCallback` for real-time data
3. Update `getCurrentHeartRate()` to use actual readings
4. Test on physical device with heart rate sensor

Example:

```kotlin
suspend fun startMeasuring() {
    val callback = object : MeasureCallback {
        override fun onDataReceived(data: DataPointContainer) {
            val heartRate = data.getData(DataType.HEART_RATE_BPM)
            _heartRate.value = heartRate.firstOrNull()?.value?.toInt() ?: 0
        }
    }
    measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
}
```

## Troubleshooting

### Build Errors

**Error: "Unresolved reference Room"**

- Solution: Run `./gradlew sync`
- Or in Android Studio: File → Sync Project with Gradle Files

**Error: KSP not found**

- Solution: Update `app/build.gradle.kts` with correct KSP version
- Match KSP version to Kotlin version

**Error: Health Services not found**

- Solution: Ensure Google Maven repository in settings
- Sync Gradle again

### Runtime Issues

**Heart rate shows "---"**

- Normal behavior if no sensor available
- App uses simulated values for testing
- Grant BODY_SENSORS permission

**Sessions not saving**

- Check database initialization in `AppDatabase.kt`
- Verify Room dependencies in `build.gradle.kts`
- Check logcat for database errors

**CSV export fails**

- Verify storage permissions
- Check external storage is available
- Look for error in logcat

**Timer doesn't start**

- Check if ViewModel is properly initialized
- Verify coroutine scope is active
- Check logcat for exceptions

## Performance Tips

### For Users

- Close app when not in use (timer only works when app is open)
- Export CSV regularly to avoid large database
- Reset rep counter when starting new routine

### For Developers

- Database queries use Flow for efficiency
- Only 20 most recent sessions loaded by default
- Timer uses coroutines (non-blocking)
- No memory leaks in ViewModel (proper lifecycle)

## Next Steps

### Immediate Usage

1. ✅ Build the app
2. ✅ Install on device
3. ✅ Complete first breathing session
4. ✅ View session in history
5. ✅ Export data to CSV

### Advanced Usage

1. Create custom breathing routines
2. Track patterns over weeks/months
3. Analyze heart rate changes
4. Share data with health professionals

### Development Enhancements

1. Add breathing pattern guidance
2. Implement real heart rate monitoring
3. Add statistics dashboard
4. Create Wear OS companion app
5. Add cloud sync

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Health Services API](https://developer.android.com/training/wearables/health-services)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## Support

For issues or questions:

1. Check this guide first
2. Review FEATURES.md for detailed documentation
3. Check README.md for architecture details
4. Open an issue in the repository

---

**Ready to start tracking your breathing exercises!** 🧘‍♂️
