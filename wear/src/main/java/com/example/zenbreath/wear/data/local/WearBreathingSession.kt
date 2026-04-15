package com.example.zenbreath.wear.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model for a breathing session recorded on the watch.
 * Includes a sync status to track if it has been pushed to the phone.
 */
@Entity(tableName = "wear_breathing_sessions")
data class WearBreathingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val startHeartRate: Int = 0,
    val endHeartRate: Int = 0,
    val repNumber: Int = 1,
    val totalReps: Int = 1,
    val timerDuration: Long = 60000L,
    val isSynced: Boolean = false
)
