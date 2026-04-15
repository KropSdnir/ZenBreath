package com.example.zenbreath.wear.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WearSessionDao {
    @Insert
    suspend fun insertSession(session: WearBreathingSession)

    @Query("SELECT * FROM wear_breathing_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<WearBreathingSession>

    @Update
    suspend fun updateSession(session: WearBreathingSession)

    @Query("SELECT * FROM wear_breathing_sessions ORDER BY startTimestamp DESC")
    fun getAllSessions(): Flow<List<WearBreathingSession>>
}
