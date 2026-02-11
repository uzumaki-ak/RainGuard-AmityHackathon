package com.rainguard.ai.viewmodel

import app.cash.turbine.test
import com.rainguard.ai.data.local.MockDataInitializer
import com.rainguard.ai.data.local.datastore.PreferencesManager
import com.rainguard.ai.data.model.UserRole
import com.rainguard.ai.ui.screens.splash.SplashDestination
import com.rainguard.ai.ui.screens.splash.SplashViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mockDataInitializer: MockDataInitializer
    private lateinit var viewModel: SplashViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesManager = mockk(relaxed = true)
        mockDataInitializer = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `first run navigates to onboarding`() = runTest {
        coEvery { preferencesManager.isFirstRun } returns flowOf(true)
        coEvery { preferencesManager.userRole } returns flowOf(null)
        coEvery { mockDataInitializer.initializeMockData() } returns Unit

        viewModel = SplashViewModel(preferencesManager, mockDataInitializer)

        viewModel.navigationEvent.test {
            val destination = awaitItem()
            assertEquals(SplashDestination.Onboarding, destination)
        }

        coVerify { mockDataInitializer.initializeMockData() }
    }

    @Test
    fun `not first run with citizen role navigates to home`() = runTest {
        coEvery { preferencesManager.isFirstRun } returns flowOf(false)
        coEvery { preferencesManager.userRole } returns flowOf(UserRole.CITIZEN)
        coEvery { mockDataInitializer.initializeMockData() } returns Unit

        viewModel = SplashViewModel(preferencesManager, mockDataInitializer)

        viewModel.navigationEvent.test {
            val destination = awaitItem()
            assertEquals(SplashDestination.HomeMap, destination)
        }
    }
}