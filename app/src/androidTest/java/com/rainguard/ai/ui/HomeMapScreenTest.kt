package com.rainguard.ai.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rainguard.ai.ui.screens.home.HomeMapScreen
import com.rainguard.ai.ui.theme.RainGuardAITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeMapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeMapScreen_displaysTitle() {
        composeTestRule.setContent {
            RainGuardAITheme {
                val navController = rememberNavController()
                HomeMapScreen(navController = navController)
            }
        }

        composeTestRule
            .onNodeWithText("RainGuardAI")
            .assertIsDisplayed()
    }

    @Test
    fun homeMapScreen_hasFABButtons() {
        composeTestRule.setContent {
            RainGuardAITheme {
                val navController = rememberNavController()
                HomeMapScreen(navController = navController)
            }
        }

        // Check for FAB buttons by content description
        composeTestRule
            .onNodeWithContentDescription("Report")
            .assertIsDisplayed()
    }
}