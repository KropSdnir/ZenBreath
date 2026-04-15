package com.example.zenbreath.data.sync

import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearSyncManager @Inject constructor(
    private val dataClient: DataClient,
    private val messageClient: MessageClient
) {
    private val _startMessageFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val startMessageFlow: SharedFlow<Unit> = _startMessageFlow.asSharedFlow()

    private val _stopMessageFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val stopMessageFlow: SharedFlow<Unit> = _stopMessageFlow.asSharedFlow()

    fun emitStartMessage() {
        _startMessageFlow.tryEmit(Unit)
    }

    fun emitStopMessage() {
        _stopMessageFlow.tryEmit(Unit)
    }
    fun observeHeartRate(): Flow<Int> = callbackFlow {
        Log.d("WearSyncManager", "Starting observeHeartRate on phone")
        val listener = DataClient.OnDataChangedListener { dataEvents ->
            Log.d("WearSyncManager", "onDataChanged: received ${dataEvents.count} events")
            try {
                for (event in dataEvents) {
                    val path = event.dataItem.uri.path
                    Log.d("WearSyncManager", "Event path: $path, type: ${if (event.type == DataEvent.TYPE_CHANGED) "CHANGED" else "DELETED"}")
                    if (event.type == DataEvent.TYPE_CHANGED && 
                        path == SyncConstants.PATH_HEART_RATE_LIVE) {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val bpm = dataMap.getInt("bpm")
                        Log.d("WearSyncManager", "Received BPM: $bpm")
                        if (bpm > 0) {
                            trySend(bpm)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WearSyncManager", "Error processing data events", e)
            } finally {
                dataEvents.release()
            }
        }

        dataClient.addListener(listener)
        awaitClose { 
            Log.d("WearSyncManager", "Stopping observeHeartRate on phone")
            dataClient.removeListener(listener) 
        }
    }

    suspend fun sendStartMessage() {
        try {
            val nodes = Wearable.getNodeClient(dataClient.applicationContext).connectedNodes.await()
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, SyncConstants.MESSAGE_START_SESSION, null).await()
            }
        } catch (e: Exception) {
            Log.e("WearSyncManager", "Failed to send start message", e)
        }
    }

    suspend fun sendStopMessage() {
        try {
            val nodes = Wearable.getNodeClient(dataClient.applicationContext).connectedNodes.await()
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, SyncConstants.MESSAGE_STOP_SESSION, null).await()
            }
        } catch (e: Exception) {
            Log.e("WearSyncManager", "Failed to send stop message", e)
        }
    }

    suspend fun updateActiveSession(isActive: Boolean, startTime: Long) {
        val request = PutDataMapRequest.create(SyncConstants.PATH_SESSION_ACTIVE).apply {
            dataMap.putBoolean(SyncConstants.KEY_SESSION_ACTIVE_STATUS, isActive)
            dataMap.putLong(SyncConstants.KEY_SESSION_START_TIME, startTime)
            setUrgent()
        }.asPutDataRequest()
        
        try {
            dataClient.putDataItem(request).await()
            Log.d("WearSyncManager", "Active session updated")
        } catch (e: Exception) {
            Log.e("WearSyncManager", "Failed to update", e)
        }
    }

    suspend fun sendHeartRate(bpm: Int) {
        val request = PutDataMapRequest.create(SyncConstants.PATH_HEART_RATE_LIVE).apply {
            dataMap.putInt("bpm", bpm)
            dataMap.putLong("timestamp", System.currentTimeMillis())
            setUrgent()
        }.asPutDataRequest()
        
        try {
            dataClient.putDataItem(request).await()
        } catch (e: Exception) {
            Log.e("WearSyncManager", "Failed to send heart rate", e)
        }
    }
}
