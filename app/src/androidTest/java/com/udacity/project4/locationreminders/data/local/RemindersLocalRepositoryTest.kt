package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var localDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase


    @Before
    fun setup() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun repositoryTest_saveReminder_retrievesReminder() = runBlocking {
        // Given a reminder
        val reminder = ReminderDTO(
            title = "Lulu Market",
            description = "Lulu market for shopping",
            location = "LuLu Hypermarket - Emerald Plaza",
            latitude = 30.05336397753033,
            longitude = 31.43075823783874,
            id = "30.05336397753033, 31.43075823783874"
        )

        localDataSource.saveReminder(reminder)

        // When reminder is retrieved by ID.
        val loaded = localDataSource.getReminder(reminder.id)

        // Then Same reminder is returned.
        loaded as Result.Success
        assertEquals(loaded.data.id, reminder.id)
        assertEquals(loaded.data.title, "Lulu Market")
        assertEquals(loaded.data.description, "Lulu market for shopping")
        assertEquals(loaded.data.location, "LuLu Hypermarket - Emerald Plaza")
        assertEquals(loaded.data.latitude, 30.05336397753033)
        assertEquals(loaded.data.longitude, 31.43075823783874)
    }

    @Test
    fun repositoryTest_saveReminder_returnDataNotFound() = runBlocking {
        // Given a reminder
        val reminder = ReminderDTO(
            title = "Lulu Market",
            description = "Lulu market for shopping",
            location = "LuLu Hypermarket - Emerald Plaza",
            latitude = 30.05336397753033,
            longitude = 31.43075823783874,
            id = "30.05336397753033, 31.43075823783874"
        )

        localDataSource.saveReminder(reminder)

        // When reminder is retrieved by ID.
        val loaded: Result<ReminderDTO> =
            localDataSource.getReminder("31.05336397753033, 31.43075823783874")

        // Then Result returns error
        assertEquals(loaded, Result.Error("Reminder not found!"))
    }
}