package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.RequestCodes
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    private fun isFineCoarseGranted() = PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

    private fun isBackgroundGranted() = PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

    @SuppressLint("MissingPermission")
    private fun requestUserPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            if (!isFineCoarseGranted() || !isBackgroundGranted()) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    RequestCodes.Q_OR_HIGHER.code
                )
            }
        } else {
            if (!isFineCoarseGranted()) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    RequestCodes.FORECOARSECODE.code
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isFineCoarseGranted() && isBackgroundGranted())
                return true
        } else {
            if (isFineCoarseGranted())
                return true
        }

        return false
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestCodes.FORECOARSECODE.code) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permissions is required")
                        .setMessage("To use the application features, you should accept the location permissions")
                        .setPositiveButton("Ok") { _, _ ->
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                RequestCodes.FORECOARSECODE.code
                            )
                        }.show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location permissions is need to use app features!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }
        } else if (requestCode == RequestCodes.Q_OR_HIGHER.code) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Allow all time permissions is required")
                    .setMessage("Please allow locations all the time to use the application features")
                    .setPositiveButton("Ok") { _, _ ->
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            RequestCodes.BACKGROUND_LOCATION_REQUEST_CODE.code
                        )
                    }.show()
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permissions is required")
                        .setMessage("To use the application features, you should accept the location permissions")
                        .setPositiveButton("Ok") { _, _ ->
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                RequestCodes.Q_OR_HIGHER.code
                            )
                        }.show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location permissions is need to use app features!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else if (requestCode == RequestCodes.BACKGROUND_LOCATION_REQUEST_CODE.code) {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {

            var isReady = false

            requestUserPermission()

            val requestUserPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isFineCoarseGranted() && isBackgroundGranted()
            } else {
                isFineCoarseGranted()
            }

            if (!requestUserPermission) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        if (!(isFineCoarseGranted() && isBackgroundGranted()))
                            Toast.makeText(
                                requireContext(),
                                "Please enable location permissions and location service",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (!isFineCoarseGranted())
                            Toast.makeText(
                                requireContext(),
                                "Please enable location permissions and location service",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            } else {

                checkDeviceLocationSettingsAndStartGeofence()

                locationSettingsResponseTask.addOnCompleteListener {
                    println("isSuccessful " + it.isSuccessful)
                    if (it.isSuccessful) {
                        val title = _viewModel.reminderTitle.value
                        val description = _viewModel.reminderDescription.value
                        val location = _viewModel.reminderSelectedLocationStr.value
                        val latitude = _viewModel.latitude.value
                        val longitude = _viewModel.longitude.value

                        val reminderDataItem = ReminderDataItem(
                            title,
                            description,
                            location,
                            latitude,
                            longitude,
                            "$latitude, $longitude"
                        )

                        _viewModel.validateAndSaveReminder(reminderDataItem)

                    }
                }
            }
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
        }
    }

    private val REQUEST_TURN_DEVICE_LOCATION_ON: Int = 12345
    private lateinit var locationSettingsResponseTask: Task<LocationSettingsResponse>

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    this.startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    println("Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
