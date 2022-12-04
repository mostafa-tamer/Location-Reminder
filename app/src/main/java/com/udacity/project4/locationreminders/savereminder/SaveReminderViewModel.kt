package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            viewModelScope.launch {
                saveReminder(reminderData)
                addingGeofence(reminderData)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun addingGeofence(reminderDataItem: ReminderDataItem) {

        val geofenceClint = LocationServices.getGeofencingClient(app.applicationContext)

        val geofenceHelper = GeofenceHelper(app.applicationContext)

        viewModelScope.launch {

            val list = dataSource.getReminders()

            if (list is Result.Success<*>) {
                val listData: List<ReminderDTO> = list.data as List<ReminderDTO>

                println(listData.size)

                val listOfGeofences = mutableListOf<Geofence>()

                for (i in listData) {
                    listOfGeofences.add(
                        geofenceHelper.getGeofence(
                            i.id,
                            LatLng(i.latitude!!, i.longitude!!),
                            150f,
                            Geofence.GEOFENCE_TRANSITION_ENTER
                        )
                    )
                }

                val geofenceRequest = geofenceHelper.getGeofenceRequest(listOfGeofences)
                val geofencePendingIntent = geofenceHelper.getPendingIntent()

                geofenceClint.addGeofences(geofenceRequest, geofencePendingIntent)
                    .addOnSuccessListener {
                        println("Geofence Is Added!")
                        println(reminderDataItem.title.toString())
                    }.addOnFailureListener {
                        print("Failed To Add Geofence!: " + it.message)
                    }
            }
        }
    }

    /**
     * Save the reminder to the data source
     */
    suspend fun saveReminder(reminderData: ReminderDataItem) {

        showLoading.value = true

        dataSource.saveReminder(
            ReminderDTO(
                reminderData.title,
                reminderData.description,
                reminderData.location,
                reminderData.latitude,
                reminderData.longitude,
                reminderData.id
            )
        )
        showLoading.value = false
        showToast.value = app.getString(R.string.reminder_saved)
        navigationCommand.value = NavigationCommand.Back
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}