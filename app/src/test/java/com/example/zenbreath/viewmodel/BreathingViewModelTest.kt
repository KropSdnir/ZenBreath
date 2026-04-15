package com.example.zenbreath.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.zenbreath.data.AppDatabase
import com.example.zenbreath.data.BreathingSessionDao
import com.example.zenbreath.service.HeartRateService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BreathingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val application = mockk<Application>(relaxed = true)
    private val database = mockk<AppDatabase>(relaxed = true)
    private val dao = mockk<BreathingSessionDao>(relaxed = true)
    private val heartRateService = mockk<HeartRateService>(relaxed = true)
    
    private lateinit var viewModel: BreathingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkObject(AppDatabase)
        every { AppDatabase.getDatabase(any()) } returns database
        every { database.breathingSessionDao() } returns dao
        
        viewModel = BreathingViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `startExercise sets isRunning to true and increments currentRep`() = runTest {
        assertEquals(0, viewModel.currentRep.value)
        assertFalse(viewModel.isRunning.value)

        viewModel.startExercise()

        assertTrue(viewModel.isRunning.value)
        assertEquals(1, viewModel.currentRep.value)
    }

    @Test
    fun `stopExercise sets isRunning to false`() = runTest {
        viewModel.startExercise()
        assertTrue(viewModel.isRunning.value)

        viewModel.stopExercise()

        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun `timer updates remainingTime over time`() = runTest {
        viewModel.setTimerDuration(5000L)
        viewModel.startExercise()
        
        assertEquals(5000L, viewModel.remainingTime.value)
        
        advanceTimeBy(1100L)
        assertEquals(4000L, viewModel.remainingTime.value)
        
        advanceTimeBy(4000L)
        assertEquals(0L, viewModel.remainingTime.value)
        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun `resetReps resets currentRep to 0`() = runTest {
        viewModel.startExercise()
        assertEquals(1, viewModel.currentRep.value)
        
        viewModel.stopExercise()
        viewModel.resetReps()
        
        assertEquals(0, viewModel.currentRep.value)
    }
}
