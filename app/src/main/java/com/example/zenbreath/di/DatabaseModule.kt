package com.example.zenbreath.di

import android.content.Context
import com.example.zenbreath.data.ZenBreathDatabase
import com.example.zenbreath.data.ZenBreathRepository
import com.example.zenbreath.data.ZenBreathSessionDao
import com.example.zenbreath.data.ZenBreathWorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ZenBreathDatabase {
        return ZenBreathDatabase.getDatabase(context)
    }

    @Provides
    fun provideZenBreathSessionDao(database: ZenBreathDatabase): ZenBreathSessionDao {
        return database.zenBreathSessionDao()
    }

    @Provides
    fun provideZenBreathWorkoutDao(database: ZenBreathDatabase): ZenBreathWorkoutDao {
        return database.zenBreathWorkoutDao()
    }

    @Provides
    @Singleton
    fun provideRepository(
        sessionDao: ZenBreathSessionDao,
        workoutDao: ZenBreathWorkoutDao
    ): ZenBreathRepository {
        return ZenBreathRepository(sessionDao, workoutDao)
    }
}
