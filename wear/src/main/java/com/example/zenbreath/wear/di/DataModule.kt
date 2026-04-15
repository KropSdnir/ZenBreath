package com.example.zenbreath.wear.di

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.room.Room
import com.example.zenbreath.wear.data.local.WearDatabase
import com.example.zenbreath.wear.data.local.WearSessionDao
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
    fun provideWearDatabase(@ApplicationContext context: Context): WearDatabase {
        return Room.databaseBuilder(
            context,
            WearDatabase::class.java,
            "wear_zenbreath.db"
        ).build()
    }

    @Provides
    fun provideWearSessionDao(database: WearDatabase): WearSessionDao {
        return database.wearSessionDao()
    }
}
