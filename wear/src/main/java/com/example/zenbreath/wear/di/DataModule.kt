package com.example.zenbreath.wear.di

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.room.Room
import com.example.zenbreath.wear.data.local.ZenBreathWatchDatabase
import com.example.zenbreath.wear.data.local.ZenBreathWatchSessionDao
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataClient(@ApplicationContext context: Context): DataClient {
        return Wearable.getDataClient(context)
    }

    @Provides
    @Singleton
    fun provideMessageClient(@ApplicationContext context: Context): MessageClient {
        return Wearable.getMessageClient(context)
    }

    @Provides
    @Singleton
    fun provideHealthServicesClient(@ApplicationContext context: Context): HealthServicesClient {
        return HealthServices.getClient(context)
    }

    @Provides
    @Singleton
    fun provideZenBreathWatchDatabase(@ApplicationContext context: Context): ZenBreathWatchDatabase {
        return Room.databaseBuilder(
            context,
            ZenBreathWatchDatabase::class.java,
            "zenbreath_watch.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideZenBreathWatchSessionDao(database: ZenBreathWatchDatabase): ZenBreathWatchSessionDao {
        return database.zenBreathSessionDao()
    }
}
