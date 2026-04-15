package com.example.zenbreath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for breathing sessions
 */
@Dao
interface BreathingSessionDao {

    @Insert
    suspend fun insert(session: BreathingSession): Long

    @Query("SELECT * FROM breathing_sessions ORDER BY startTimestamp DESC")
    fun getAllSessions(): Flow<List<BreathingSession>>

    @Query("SELECT * FROM breathing_sessions ORDER BY startTimestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<BreathingSession>>

    @Query("SELECT * FROM breathing_sessions WHERE startTimestamp >= :startOfDay AND startTimestamp < :endOfDay ORDER BY startTimestamp DESC")
    fun getSessionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<BreathingSession>>

    @Query("DELETE FROM breathing_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Long)

    @Query("DELETE FROM breathing_sessions")
    suspend fun deleteAll()
}
