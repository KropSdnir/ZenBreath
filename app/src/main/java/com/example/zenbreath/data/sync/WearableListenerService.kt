package com.example.zenbreath.data.sync

import android.util.Log
import com.example.zenbreath.data.BreathingRepository
import com.example.zenbreath.data.BreathingSession
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ZenBreathWearableListenerService : WearableListenerService() {

    @Inject
    lateinit var repository: BreathingRepository

    @Inject
    lateinit var wearSyncManager: WearSyncManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            SyncConstants.MESSAGE_START_SESSION -> {
                Log.d("WearableService", "Start session message received")
                wearSyncManager.emitStartMessage()
                
                // Launch MainActivity to ensure the UI is visible
                val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                intent?.let { startActivity(it) }
            }
            SyncConstants.MESSAGE_STOP_SESSION -> {
                Log.d("WearableService", "Stop session message received")
                wearSyncManager.emitStopMessage()
                scope.launch {
                    wearSyncManager.updateActiveSession(false, 0)
                }
            }
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            Log.d("WearableService", "Data changed: ${dataEvents.count} events")
            for (event in dataEvents) {
                val path = event.dataItem.uri.path
                Log.d("WearableService", "Event path: $path")
                
                if (event.type == DataEvent.TYPE_CHANGED && 
                    path == SyncConstants.PATH_HEART_RATE_LIVE) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val bpm = dataMap.getInt("bpm")
                    Log.d("WearableService", "Live BPM received: $bpm")
                }
                
                if (event.type == DataEvent.TYPE_CHANGED && 
                    path == SyncConstants.PATH_SESSION_COMPLETE) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    
                    val session = BreathingSession(
                        startTimestamp = dataMap.getLong("start_timestamp"),
                        endTimestamp = dataMap.getLong("end_timestamp"),
                        startHeartRate = dataMap.getInt("start_heart_rate"),
                        endHeartRate = dataMap.getInt("end_heart_rate"),
                        repNumber = dataMap.getInt("rep_number"),
                        totalReps = dataMap.getInt("total_reps"),
                        timerDuration = dataMap.getLong("timer_duration")
                    )
                    
                    scope.launch {
                        repository.insertSession(session)
                        Log.d("WearableService", "Synced session from watch to phone")
                    }
                }
            }
        } finally {
            dataEvents.release()
        }
    }
}
