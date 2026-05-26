package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LockedIn", appName)
    }

    @Test
    fun `test default commitment state`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)

        // Default should be not set
        assertEquals(-1, viewModel.commitmentDays.value)
        assertEquals(-1L, viewModel.commitmentEndTimestamp.value)
        assertFalse(viewModel.commitmentSet.value ?: true)
    }

    @Test
    fun `test saving study commitment`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)

        // Save 45 days commitment
        viewModel.saveCommitment(45)

        assertEquals(45, viewModel.commitmentDays.value)
        assertTrue(viewModel.commitmentSet.value ?: false)
        assertTrue((viewModel.commitmentEndTimestamp.value ?: 0) > System.currentTimeMillis())

        // Clear commitment
        viewModel.clearCommitment()
        assertEquals(-1, viewModel.commitmentDays.value)
        assertFalse(viewModel.commitmentSet.value ?: true)
    }

    @Test
    fun `test weekly study statistics`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(application)

        var weeklyMinutes: Int? = null
        var weeklySessions: Int? = null

        viewModel.weeklyStudyMinutes.observeForever { weeklyMinutes = it }
        viewModel.weeklySessionsCompleted.observeForever { weeklySessions = it }

        // Process any initialization
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        // Initial state before DB finishes loading Flow might be null, but if populated must be 0
        assertTrue(weeklyMinutes == null || weeklyMinutes == 0)
        assertTrue(weeklySessions == null || weeklySessions == 0)
    }
}
