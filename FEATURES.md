# ZenBreath - Detailed Features Documentation

## App Overview

ZenBreath is a comprehensive breathing exercise tracking application designed to help users monitor
their breathing exercises with precise timing, heart rate tracking, and detailed session history.

## Main Screen Components

### 1. Header Section

- **Date Display**: Shows current date in MM/DD/YYYY format
- Updates automatically to current date

### 2. Timer Display

- **Visual Design**: Large circular progress ring
    - Purple progress arc that depletes as time counts down
    - Light gray background circle
    - Central countdown display in MM:SS format
    - 200dp diameter for clear visibility

- **Behavior**:
    - Counts down from preset time
    - Updates every second
    - Visual progress ring animates smoothly
    - Auto-stops when reaching 00:00

### 3. Timer Configuration

- **Display**: "Timer: XXs" with edit icon
- **Options**:
    - 30 seconds
    - 1 minute (default: 60 seconds)
    - 2 minutes
    - 3 minutes
    - 5 minutes
- **Interaction**:
    - Tap edit icon to open selection dialog
    - Currently selected option highlighted in purple
    - Cannot change during active session

### 4. Repetition Configuration

- **Display**: "Rep: XX" with edit icon
- **Options**: 5, 10 (default), 15, 20, 25, 30 reps
- **Interaction**:
    - Tap edit icon to open selection dialog
    - Currently selected option highlighted in purple
    - Cannot change during active session

### 5. Heart Rate Display

- **Display**: "Heart Rate: XXX BPM" or "---" if not available
- **Color**: Purple (primary color)
- **Behavior**:
    - Shows "---" before first session
    - Updates when START is pressed
    - Simulates heart rate (60-100 BPM) if sensor unavailable
    - In production, would use actual heart rate sensor

### 6. START/STOP Button

- **Normal State** (START):
    - Purple background
    - Full-width button
    - Bold white text "START"
    - 56dp height

- **Active State** (STOP):
    - Red background
    - Full-width button
    - Bold white text "STOP"
    - 56dp height

- **Behavior**:
    - START: Begins countdown timer
        - Captures start timestamp
        - Records start heart rate
        - Increments rep counter
        - Disables configuration controls
    - STOP: Ends session
        - Captures end timestamp
        - Records end heart rate
        - Saves session to database
        - Re-enables configuration controls

### 7. Rep Counter

- **Display**: "Rep Count: X/Y" where:
    - X = current rep number
    - Y = total reps configured
- **Reset Button**: Refresh icon to reset counter to 0
- **Behavior**:
    - Increments automatically when START is pressed
    - Can be reset manually when not running
    - Persists across app restarts

### 8. Session History

- **Header**: "Session History" (bold)
- **List Layout**:
    - Scrollable vertical list
    - Most recent sessions at top (descending order)
    - Each session in a card with light background

- **Session Card Format**:
  ```
  [Index]. S:HH:MM E:HH:MM | SHR: XXX EHR: XXX
  ```
    - Index: Sequential number (1, 2, 3...)
    - S: Start time in HH:MM format
    - E: End time in HH:MM format
    - SHR: Start Heart Rate in BPM
    - EHR: End Heart Rate in BPM

- **Deletion**:
    - Long-press on any session to delete
    - Haptic feedback on long-press
    - Confirmation dialog before deletion
    - Session removed from database immediately

- **Example**:
  ```
  2. S:22:14 E:22:45 | SHR: 72 EHR: 68
  1. S:22:14 E:22:45 | SHR: 75 EHR: 71
  ```

### 9. Export Button (FAB)

- **Design**: Floating Action Button with share icon
- **Position**: Bottom-right corner
- **Behavior**:
    - Tap to export all sessions to CSV
    - Shows toast message with file path
    - File saved to app's external storage
    - Can be shared or opened with other apps

## User Workflows

### Starting a Breathing Session

1. Open app (shows default settings: 60s timer, 10 reps)
2. (Optional) Tap timer edit icon to change duration
3. (Optional) Tap rep edit icon to change rep count
4. Tap START button
5. App captures:
    - Start timestamp (milliseconds)
    - Start heart rate (current reading)
6. Timer begins countdown
7. Button changes to red STOP
8. Configuration controls disabled
9. Rep counter increments (e.g., 0/10 → 1/10)

### During a Session

1. Timer counts down each second
2. Progress ring visually depletes
3. Heart rate updated continuously
4. User can manually stop by tapping STOP
5. Or wait for timer to reach 00:00 (auto-stops)

### Ending a Session

1. Timer reaches 00:00 OR user taps STOP
2. App captures:
    - End timestamp (milliseconds)
    - End heart rate (current reading)
3. Session saved to database with:
    - Start/End timestamps
    - Start/End heart rates
    - Rep number (e.g., 1 of 10)
    - Total reps configured (e.g., 10)
    - Timer duration (e.g., 60000ms)
4. New session appears at top of history list
5. Configuration controls re-enabled
6. Timer resets to configured duration
7. Button changes back to purple START

### Viewing History

1. Scroll down past controls
2. See "Session History" header
3. View all past sessions in descending order
4. Each entry shows complete session data
5. No limit on history (all sessions stored)

### Exporting Data

1. Tap share icon (floating button bottom-right)
2. App generates CSV file with all sessions
3. Toast message shows file path
4. File includes:
    - Header row with column names
    - One row per session
    - Timestamps in readable format
    - All heart rate data
    - Rep information
    - Duration in seconds

### Deleting Sessions

1. Long-press on any session in the history list
2. Feel haptic feedback confirming long-press
3. Confirmation dialog appears showing session details
4. Choose "Delete" to confirm or "Cancel" to keep
5. Session permanently removed from database
6. History list updates automatically

### Resetting Rep Count

1. Ensure no session is running
2. Tap refresh icon next to "Rep Count"
3. Counter resets to 0/X
4. Start fresh rep sequence

## Data Persistence

### Database Schema

```kotlin
BreathingSession Entity:
- id: Long (auto-generated primary key)
- startTimestamp: Long (milliseconds since epoch)
- endTimestamp: Long (milliseconds since epoch)
- startHeartRate: Int (BPM)
- endHeartRate: Int (BPM)
- repNumber: Int (which rep, e.g., 1)
- totalReps: Int (total planned, e.g., 10)
- timerDuration: Long (milliseconds)
```

### Storage Locations

1. **Database**:
    - SQLite database via Room
    - Path: /data/data/com.example.zenbreath/databases/zen_breath_database
    - Persistent across app restarts

2. **CSV Exports**:
    - Path: /Android/data/com.example.zenbreath/files/
    - Filename: breathing_sessions_[timestamp].csv
    - Accessible via file manager
    - Can be shared/moved

## Technical Implementation Details

### State Management

- **ViewModel**: Single source of truth for UI state
- **StateFlow**: Reactive state updates to UI
- **Coroutines**: Asynchronous operations (database, timer)

### Timer Implementation

- Countdown using Kotlin Coroutines
- 1-second intervals (1000ms delay)
- Cancellable job for stop functionality
- Auto-stop when reaching zero

### Heart Rate Simulation

- Random values between 60-100 BPM
- New value each second during session
- Realistic for testing without physical sensor
- Framework ready for real sensor integration

### CSV Export Format

```csv
ID,Start Time,End Time,Start Heart Rate,End Heart Rate,Rep Number,Total Reps,Duration (seconds)
1,2025-01-06 14:30:00,2025-01-06 14:31:00,72,68,1,10,60
2,2025-01-06 14:32:15,2025-01-06 14:33:15,75,71,2,10,60
```

## UI/UX Features

### Material Design 3

- Modern color scheme with purple primary
- Consistent spacing and typography
- Touch-friendly button sizes (56dp height)
- Clear visual hierarchy

### Accessibility

- Large text for timer (48sp)
- High contrast colors
- Touch targets meet minimum size
- Clear visual feedback for all interactions

### Responsive Design

- Adapts to different screen sizes
- Scrollable content
- No horizontal scrolling needed
- Proper padding and margins

### User Feedback

- Button color changes (purple ↔ red)
- Timer countdown visible
- Toast messages for exports
- Disabled state for controls during session

## Error Handling

### Heart Rate Sensor

- Checks availability on app start
- Falls back to simulation if unavailable
- Logs errors for debugging
- User not blocked from using app

### Database Operations

- All operations in coroutines (non-blocking)
- Error handling in repository layer
- Graceful degradation if write fails

### Permission Handling

- Requests permissions on first launch
- Shows toast if permissions denied
- App functional even without all permissions
- Heart rate may use simulation mode

## Performance Considerations

### Efficiency

- Lightweight timer implementation
- Database queries optimized with Room
- Only shows recent 20 sessions by default
- Lazy loading for session list

### Battery

- No background services
- Timer only runs when app active
- No continuous heart rate monitoring
- Minimal wake locks

### Memory

- Flow-based data loading
- No memory leaks in ViewModel
- Proper lifecycle handling
- Automatic cleanup on destroy

## Summary of Completed Requirements

✅ **Countdown Timer**: Implemented with visual progress ring  
✅ **Customizable Duration**: 5 preset options, editable via dialog  
✅ **Start Timestamp**: Captured on START button tap  
✅ **Start Heart Rate**: Recorded at session start  
✅ **End Timestamp**: Captured on STOP or timer completion  
✅ **End Heart Rate**: Recorded at session end  
✅ **Home Screen Layout**: Matches specification exactly  
✅ **Rep Counter**: Selectable, displays X/Y format  
✅ **Session History**: Descending order with all required data  
✅ **CSV Export**: Full data export functionality  
✅ **Database Storage**: Room database with proper schema

All requirements from the original specification have been fully implemented!
