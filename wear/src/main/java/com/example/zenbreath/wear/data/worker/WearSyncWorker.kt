package com.example.zenbreath.wear.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenbreath.wear.data.local.ZenBreathWatchSessionDao
import com.example.zenbreath.wear.data.sync.ZenBreathSyncConstants
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

class WearSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionDao: ZenBreathWatchSessionDao,
    private val dataClient: DataClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedSessions = sessionDao.getUnsyncedSessions()
            
            for (session in unsyncedSessions) {
                val request = PutDataMapRequest.create(ZenBreathSyncConstants.PATH_SESSION_COMPLETE).apply {
                    dataMap.putLong("start_timestamp", session.startTimestamp)
                    dataMap.putLong("end_timestamp", session.endTimestamp)
                    dataMap.putInt("start_heart_rate", session.startHeartRate)
                    dataMap.putInt("end_heart_rate", session.endHeartRate)
                    dataMap.putInt("rep_number", session.repNumber)
                    dataMap.putInt("total_reps", session.totalReps)
                    dataMap.putLong("timer_duration", session.timerDuration)
                    dataMap.putInt("min_heart_rate", session.minHeartRate)
                    dataMap.putInt("max_heart_rate", session.maxHeartRate)
                    dataMap.putLong("sync_timestamp", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()
                
                dataClient.putDataItem(request).await()
                
                // Mark as synced locally
                sessionDao.updateSession(session.copy(isSynced = true))
                Log.d("ZenBreathWearSyncWorker", "Synced session ${session.id} to data layer")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("ZenBreathWearSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}
