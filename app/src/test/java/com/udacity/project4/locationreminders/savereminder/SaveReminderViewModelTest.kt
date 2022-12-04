package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import castingProperty
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val reminder1 =
        ReminderDTO("Title1", "Description1", "Location1", 30.92456, 32.1543, "30.92456, 32.1543")
    private val reminder2 =
        ReminderDTO("Title2", "Description2", "Location2", 31.92456, 33.1543, "31.92456, 33.1543")
    private val reminder3 =
        ReminderDTO("Title3", "Description3", "Location3", 32.92456, 34.1543, "32.92456, 34.1543")

    private val listOfReminderDTO = listOf(reminder1, reminder2, reminder3)

    private val fakeDataSource = FakeDataSource(listOfReminderDTO.toMutableList())

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private val applicationContext: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setUpSaveReminderViewModel() {
        saveReminderViewModel =
            SaveReminderViewModel(applicationContext, fakeDataSource)
    }

    @After
    fun clear() {
        stopKoin()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun saveReminder_getReminder_test() = mainCoroutineRule.runBlockingTest {

        //Given a reminderDataItem
        val reminderDataItem = ReminderDataItem(
            "Title Test",
            "Description Test",
            "Location Test",
            12345.54321,
            12345.54321,
            "ID Test"
        )

        //When saving reminderDataItem to the fake dataSource
        saveReminderViewModel.saveReminder(reminderDataItem)

        //Then ensure that the data is saved into the fake dataSource
        assertEquals(
            castingProperty(reminderDataItem),
            (fakeDataSource.getReminder(reminderDataItem.id) as Result.Success).data
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun savingReminderTest_savingInFakeDataSource_getTheReminderNotSaved() = mainCoroutineRule.runBlockingTest {
        //Given a reminderDataItem
        val reminderDataItem = ReminderDataItem(
            "",
            "Description Test",
            "",
            12345.54321,
            12345.54321,
            "ID Test 2"
        )

        var isFound = false

        //When saving reminderDataItem to the fake dataSource
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        //Then ensure that the data isn't saved into the fake dataSource because it is violating the reminder's saving rules

        val listOfReminders = fakeDataSource.getReminders() as Result.Success<List<ReminderDTO>>
        for (i in listOfReminders.data)
            if (i.id == "ID Test 2")
                isFound = true

        assertEquals(
            isFound,
            false
        )
    }

    @Test
    fun snackBarTest_insertInvalidReminder_returnSnackBarValueAsAnErrorFlag() {

        //Given a reminderDataItem with no title to make the value of the snackBarIntChange
        val reminderDataItem = ReminderDataItem(
            "",
            "Description Test",
            "Location Test",
            12345.54321,
            12345.54321,
            "ID Test 3"
        )

        //Then observe the showSnackBarInt object and add a reminderDataItem without title to change the value of the showSnackBarInt
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        //Then asserting that the value of showSnackBarInt is changed and not equal null
        assertNotNull(saveReminderViewModel.showSnackBarInt.value)
    }

    @Test
    fun toastTest_insertReminder_getReminderSaved() {
        //Given a reminderDataItem to change by adding it to the fake datasource the value of the showToast
        val reminderDataItem = ReminderDataItem(
            "Title Test",
            "Description Test",
            "Location Test",
            132345.54321,
            132345.54321,
            "ID Test 4"
        )

        //Then observe the showToast object and add a reminderDataItem to change the value of the showToast
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        saveReminderViewModel.showToast.getOrAwaitValue()

        //Then asserting that the value of showToast is changed and not equal null
        assertNotNull(saveReminderViewModel.showToast.value)
    }
}

