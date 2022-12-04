package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun daoTest_insertReminderAndGetItById() = runBlockingTest {
        // GIVEN a reminder
        val reminder = ReminderDTO(
            title = "Lulu Market",
            description = "Lulu market for shopping",
            location = "LuLu Hypermarket - Emerald Plaza",
            latitude = 30.05336397753033,
            longitude = 31.43075823783874,
            id = "30.05336397753033, 31.43075823783874"
        )

        database.reminderDao().saveReminder(reminder)

        // WHEN load the data from the database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN
        assertNotNull(loaded as ReminderDTO)
        assertEquals(loaded.id, reminder.id)
        assertEquals(loaded.title, reminder.title)
        assertEquals(loaded.description, reminder.description)
        assertEquals(loaded.location, reminder.location)
        assertEquals(loaded.latitude, reminder.latitude)
        assertEquals(loaded.longitude, reminder.longitude)
    }
}