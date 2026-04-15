package com.example.zenbreath

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSimpleText() {
        composeTestRule.setContent {
            Text("Hello World")
        }
        composeTestRule.onNodeWithText("Hello World").assertIsDisplayed()
    }
}
