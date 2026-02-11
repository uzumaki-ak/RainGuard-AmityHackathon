package com.rainguard.ai.viewmodel

import app.cash.turbine.test
import com.rainguard.ai.data.repository.ReportRepository
import com.rainguard.ai.data.repository.RiskZoneRepository
import com.rainguard.ai.data.repository.RouteRepository
import com.rainguard.ai.data.repository.ShelterRepository
import com.rainguard.ai.ui.screens.home.HomeMapViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeMapViewModelTest {

    private lateinit var riskZoneRepository: RiskZoneRepository
    private lateinit var shelterRepository: ShelterRepository
    private lateinit var reportRepository: ReportRepository
    private lateinit var routeRepository: RouteRepository
    private lateinit var viewModel: HomeMapViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        riskZoneRepository = mockk()
        shelterRepository = mockk()
        reportRepository = mockk()
        routeRepository = mockk()

        coEvery { riskZoneRepository.getRiskZones() } returns flowOf(emptyList())
        coEvery { shelterRepository.getShelters() } returns flowOf(emptyList())
        coEvery { reportRepository.getReports() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        viewModel = HomeMapViewModel(
            riskZoneRepository,
            shelterRepository,
            reportRepository,
            routeRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
        }
    }
}