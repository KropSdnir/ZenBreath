package com.example.zenbreath.wear.data.sync

import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearSyncManager @Inject constructor(
    private val dataClient: DataClient,
    private val messageClient: MessageClient,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {
    fun observeActiveSession(): Flow<Pair<Boolean, Long>> = callbackFlow {
        val listener = DataClient.OnDataChangedListener { dataEvents ->
            try {
                for (event in dataEvents) {
                    if (event.type == DataEvent.TYPE_CHANGED &&
                        event.dataItem.uri.path == SyncConstants.PATH_SESSION_ACTIVE
                    ) {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val isActive = dataMap.getBoolean(SyncConstants.KEY_SESSION_ACTIVE_STATUS)
                        val startTime = dataMap.getLong(SyncConstants.KEY_SESSION_START_TIME)
                        trySend(Pair(isActive, startTime))
                    }
                }
            } finally {
                dataEvents.release()
            }
        }

        dataClient.addListener(listener)
        
        // Initial value fetch
        dataClient.dataItems.addOnSuccessListener { dataItems ->
            try {
                for (item in dataItems) {
                    if (item.uri.path == SyncConstants.PATH_SESSION_ACTIVE) {
                        val dataMap = DataMapItem.fromDataItem(item).dataMap
                        trySend(Pair(
                            dataMap.getBoolean(SyncConstants.KEY_SESSION_ACTIVE_STATUS),
                            dataMap.getLong(SyncConstants.KEY_SESSION_START_TIME)
                        ))
                    }
                }
            } finally {
                dataItems.release()
            }
        }

        awaitClose { dataClient.removeListener(listener) }
    }

    fun observeHeartRate(): Flow<Int> = callbackFlow {
        val listener = DataClient.OnDataChangedListener { dataEvents ->
            try {
                for (event in dataEvents) {
                    if (event.type == DataEvent.TYPE_CHANGED &&
                        event.dataItem.uri.path == SyncConstants.PATH_HEART_RATE_LIVE
                    ) {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        trySend(dataMap.getInt("bpm"))
                    }
                }
            } finally {
                dataEvents.release()
            }
        }
        dataClient.addListener(listener)
        awaitClose { dataClient.removeListener(listener) }
    }

    suspend fun sendStartMessage() {
        sendMessage(SyncConstants.MESSAGE_START_SESSION)
    }

    suspend fun sendStopMessage() {
        sendMessage(SyncConstants.MESSAGE_STOP_SESSION)
    }

    suspend fun sendHeartRate(bpm: Int) {
        val request = com.google.android.gms.wearable.PutDataMapRequest.create(SyncConstants.PATH_HEART_RATE_LIVE).apply {
            dataMap.putInt("bpm", bpm)
            dataMap.putLong("timestamp", System.currentTimeMillis())
            // Set urgent to ensure immediate delivery
            setUrgent()
        }.asPutDataRequest()

        try {
            Log.d("WearSyncManager", "Sending Heart Rate to Phone: $bpm")
            dataClient.putDataItem(request).await()
            Log.d("WearSyncManager", "Heart Rate sent successfully")
        } catch (e: Exception) {
            Log.e("WearSyncManager", "Failed to send heart rate", e)
        }
    }

    private suspend fun sendMessage(path: String) {
        try {
            Log.d("WearSyncManager", "Sending message: $path")
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            Log.d("WearSyncManager", "Found ${nodes.size} nodes")
            for (node in nodes) {
                val result = messageClient.sendMessage(node.id, path, null).await()
                Log.d("WearSyncManager", "Message $path sent to ${node.displayName}, result: $result")
            }
        } catch (e: Exception) {
            Log.e("WearSyncManager", "Failed to send message: $path", e)
        }
    }
}
