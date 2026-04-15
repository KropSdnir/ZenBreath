package com.example.zenbreath.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.core.layout.WindowSizeClass
import com.example.zenbreath.HiltTestActivity
import com.example.zenbreath.data.BreathingRepository
import com.example.zenbreath.service.HeartRateService
import com.example.zenbreath.ui.screens.home.AdaptiveHomeScreen
import com.example.zenbreath.viewmodel.BreathingViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(AndroidJUnit4::class)
class AdaptiveHomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    private val repository = mockk<BreathingRepository>(relaxed = true)
    private val heartRateService = mockk<HeartRateService>(relaxed = true)
    private val viewModel = BreathingViewModel(repository, heartRateService)

    @Test
    fun testCompactLayout() {
        val compactSizeClass = WindowSizeClass.compute(400f, 800f)
        val compactInfo = WindowAdaptiveInfo(
            windowSizeClass = compactSizeClass,
            windowPosture = mockk(relaxed = true)
        )
        
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateText = dateFormat.format(Date())

        composeTestRule.setContent {
            MaterialTheme {
                AdaptiveHomeScreen(
                    viewModel = viewModel,
                    onTimerDurationEdit = {},
                    onRepCountEdit = {},
                    adaptiveInfo = compactInfo
                )
            }
        }
        
        // Verify that the date (HomeHeader) is displayed in compact mode
        composeTestRule.onNodeWithText(dateText).assertIsDisplayed()
    }

    @Test
    fun testExpandedLayout() {
        val expandedSizeClass = WindowSizeClass.compute(1000f, 800f)
        val expandedInfo = WindowAdaptiveInfo(
            windowSizeClass = expandedSizeClass,
            windowPosture = mockk(relaxed = true)
        )
        
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateText = dateFormat.format(Date())

        composeTestRule.setContent {
            MaterialTheme {
                AdaptiveHomeScreen(
                    viewModel = viewModel,
                    onTimerDurationEdit = {},
                    onRepCountEdit = {},
                    adaptiveInfo = expandedInfo
                )
            }
        }
        
        // In expanded mode, both HomeHeader and some text from SessionHistoryList should be visible.
        // SessionHistoryList uses "No sessions for this date" or similar if empty.
        composeTestRule.onNodeWithText(dateText).assertIsDisplayed()
    }
}
