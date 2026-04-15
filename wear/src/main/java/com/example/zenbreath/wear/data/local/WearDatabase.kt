package com.example.zenbreath.wear.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WearBreathingSession::class], version = 1)
abstract class WearDatabase : RoomDatabase() {
    abstract fun wearSessionDao(): WearSessionDao
}
