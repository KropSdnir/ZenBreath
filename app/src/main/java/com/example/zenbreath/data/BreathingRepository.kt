package com.example.zenbreath.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for managing breathing session data
 */
class BreathingRepository(private val dao: BreathingSessionDao) {

    fun getAllSessions(): Flow<List<BreathingSession>> = dao.getAllSessions()

    fun getRecentSessions(limit: Int): Flow<List<BreathingSession>> =
        dao.getRecentSessions(limit)

    fun getSessionsByDate(startOfDay: Long, endOfDay: Long): Flow<List<BreathingSession>> =
        dao.getSessionsByDate(startOfDay, endOfDay)

    suspend fun insertSession(session: BreathingSession): Long {
        return dao.insert(session)
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteById(sessionId)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    /**
     * Export all sessions to CSV file.
     * Queries the database for all records and writes them to a file.
     */
    suspend fun exportToCSV(context: Context): String {
        // Collect current snapshot of all sessions from the Flow
        val sessions = dao.getAllSessions().first()
        val csvContent = formatSessionsToCSV(sessions)

        val fileName = "breathing_all_sessions_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        // MANUAL TUNING: Ensure the app has proper storage permissions if targeting legacy versions
        file.writeText(csvContent)

        return file.absolutePath
    }

    /**
     * Format sessions list to CSV string
     */
    fun formatSessionsToCSV(sessions: List<BreathingSession>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csv = StringBuilder()

        // Header
        csv.append("ID,Start Time,End Time,Start Heart Rate,End Heart Rate,Rep Number,Total Reps,Duration (seconds)\n")

        // Data rows
        sessions.forEach { session ->
            csv.append("${session.id},")
            csv.append("${dateFormat.format(Date(session.startTimestamp))},")
            csv.append("${dateFormat.format(Date(session.endTimestamp))},")
            csv.append("${session.startHeartRate},")
            csv.append("${session.endHeartRate},")
            csv.append("${session.repNumber},")
            csv.append("${session.totalReps},")
            csv.append("${session.timerDuration / 1000}\n")
        }

        return csv.toString()
    }
}
