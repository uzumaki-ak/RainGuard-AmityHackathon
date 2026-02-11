package com.rainguard.ai.viewmodel

import android.net.Uri
import com.rainguard.ai.data.model.ReportType
import com.rainguard.ai.data.model.Result
import com.rainguard.ai.data.repository.ReportRepository
import com.rainguard.ai.ui.screens.report.ReportViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private lateinit var reportRepository: ReportRepository
    private lateinit var viewModel: ReportViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        reportRepository = mockk(relaxed = true)
        viewModel = ReportViewModel(reportRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setType updates state`() = runTest {
        viewModel.setType(ReportType.FLOOD)

        val state = viewModel.state.first()
        assertEquals(ReportType.FLOOD, state.selectedType)
    }

    @Test
    fun `setDescription updates state`() = runTest {
        viewModel.setDescription("Test description")

        val state = viewModel.state.first()
        assertEquals("Test description", state.description)
    }

    @Test
    fun `submitReport requires type and location`() = runTest {
        viewModel.submitReport()

        val state = viewModel.state.first()
        assertEquals("Please select type and location", state.error)
    }

    @Test
    fun `submitReport calls repository when valid`() = runTest {
        val mockUri: Uri = mockk()
        every { mockUri.toString() } returns "test://uri"

        coEvery {
            reportRepository.submitReport(any(), any(), any(), any(), any(), any())
        } returns Result.Success(Unit)

        viewModel.setType(ReportType.FLOOD)
        viewModel.setLocation(28.7, 77.1)
        viewModel.setDescription("Test")

        viewModel.submitReport()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            reportRepository.submitReport(
                ReportType.FLOOD,
                28.7,
                77.1,
                any(),
                "Test",
                any()
            )
        }
    }
}