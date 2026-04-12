# ZenBreath - Changelog

## Version 1.1.0 - Delete Sessions Feature

### Added

- **Long-press to delete**: Users can now long-press on any session in the history to delete it
- **Haptic feedback**: Device vibrates when long-press is detected
- **Confirmation dialog**: Shows session details before deletion to prevent accidents
- **Database delete**: Added `deleteById()` function to DAO for individual session removal
- **Repository method**: Added `deleteSession()` to repository layer
- **ViewModel support**: Added `deleteSession()` function for UI interaction

### Changed

- Updated `SessionItem` component to support `combinedClickable` with long-press
- Added `onDelete` callback parameter to `SessionItem`
- Modified `HomeScreen` to pass delete callback to session items
- Updated all documentation to reflect new deletion feature

### Technical Details

- Uses `@OptIn(ExperimentalFoundationApi::class)` for `combinedClickable`
- Haptic feedback uses `HapticFeedbackType.LongPress`
- Confirmation dialog uses Material3 `AlertDialog`
- Database deletion is async with coroutines
- UI updates automatically via Flow

### Files Modified

1. `data/BreathingSessionDao.kt` - Added deleteById query
2. `data/BreathingRepository.kt` - Added deleteSession method
3. `viewmodel/BreathingViewModel.kt` - Added deleteSession function
4. `ui/components/SessionItem.kt` - Added long-press gesture and dialog
5. `ui/screens/HomeScreen.kt` - Added onDelete callback
6. Documentation files updated

### User Experience

1. Long-press on any session in history
2. Feel haptic vibration feedback
3. See confirmation dialog with session details
4. Tap "Delete" to confirm or "Cancel" to keep
5. Session removed immediately from list

---

## Version 1.0.0 - Initial Release

### Features

- Countdown timer with visual progress ring
- Heart rate monitoring (start and end)
- Repetition tracking system
- Session history in descending order
- CSV export for data analysis
- Room database for persistent storage
- Material Design 3 UI
- Permission handling for sensors

### Components

- Data layer with Room database
- ViewModel for state management
- Jetpack Compose UI
- Health Services integration
- Repository pattern implementation

### Documentation

- README.md - Project overview
- FEATURES.md - Detailed feature documentation
- QUICK_START.md - User and developer guide
- PROJECT_SUMMARY.md - Implementation summary
