package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.savereminder.MainCoroutineRule
import getOrAwaitValue
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var reminderViewModel: RemindersListViewModel

    private val fakeDataSource = FakeDataSource()

    private val applicationContext: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setUpRemindersListViewModel() {
        reminderViewModel =
            RemindersListViewModel(applicationContext, fakeDataSource)
    }

    @After
    fun clear() {
        stopKoin()
    }

    @Test
    fun loadReminders_loading() {

        //When
        mainCoroutineRule.pauseDispatcher()
        reminderViewModel.loadReminders()

        //Then
        assertEquals(reminderViewModel.showLoading.getOrAwaitValue(), true)
        mainCoroutineRule.resumeDispatcher()

        assertEquals(reminderViewModel.showLoading.getOrAwaitValue(), false)
    }

    @Test
    fun showNoDataTest_checkDataExistent_returnResults() {

        reminderViewModel.invalidateShowNoData()

        assertEquals(reminderViewModel.showNoData.getOrAwaitValue(), true)

        reminderViewModel.remindersList.value = listOf(
            ReminderDataItem(
                "Title Test",
                "Description Test",
                "Location Test",
                132345.54321,
                132345.54321,
                "ID Test 4"
            )
        )
        reminderViewModel.invalidateShowNoData()

        assertEquals(reminderViewModel.showNoData.getOrAwaitValue(), false)
    }

    @Test
    fun errorTest_loadingReminders_returnErrorMessage() = mainCoroutineRule.runBlockingTest {

        fakeDataSource.setShouldReturnError(true)

        reminderViewModel.loadReminders()

        assertEquals(
            reminderViewModel.showSnackBar.getOrAwaitValue(),
            "Test exception"
        )
    }
}
