package com.rainguard.ai.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rainguard.ai.ui.screens.report.ReportScreen
import com.rainguard.ai.ui.theme.RainGuardAITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reportScreen_displaysTypeSelection() {
        composeTestRule.setContent {
            RainGuardAITheme {
                val navController = rememberNavController()
                ReportScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithText("Report Type")
            .assertIsDisplayed()
    }

    @Test
    fun reportScreen_hasSubmitButton() {
        composeTestRule.setContent {
            RainGuardAITheme {
                val navController = rememberNavController()
                ReportScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithText("Submit Report")
            .assertIsDisplayed()
    }
}