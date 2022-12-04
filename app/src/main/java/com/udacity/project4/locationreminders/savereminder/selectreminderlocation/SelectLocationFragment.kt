package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.RequestCodes
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    data class SelectedLocation(
        var latitude: Double? = null,
        var longitude: Double? = null,
        var poi: PointOfInterest? = null
    )

    private var selectedLocation = SelectedLocation()

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        requestPermissions()

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->

            map = googleMap

            setMapStyle(googleMap)

            checkPermissions()

            startUsingMap()
        }

        binding.saveButton.setOnClickListener {

            onLocationSelected(selectedLocation)
        }

        return binding.root
    }

    private lateinit var locationSettingsResponseTask: Task<LocationSettingsResponse>

    private val REQUEST_TURN_DEVICE_LOCATION_ON = 678912345

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

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestCodes.FORECOARSECODE.code) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permissions is required")
                        .setMessage("To use the application features, you should accept the location permissions")
                        .setPositiveButton("Ok") { _, _ ->
                            requestPermissions(
                                arrayOf(
                                    ACCESS_FINE_LOCATION,
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
                checkDeviceLocationSettingsAndStartGeofence()
                map.isMyLocationEnabled = true
            }
        }
    }

    private fun requestPermissions() {
        if (!isFineCoarseGranted()) {
            requestPermissions(
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                RequestCodes.FORECOARSECODE.code
            )
        } else
            checkDeviceLocationSettingsAndStartGeofence()
    }

    private fun isFineCoarseGranted() = PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) && PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

    private fun checkPermissions() {
        if (PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            )
        ) {
            map.isMyLocationEnabled = true
        }
    }

    private fun addingCircle(latLng: LatLng, color: Int = Color.argb(65, 255, 0, 0)) {

        map.addCircle(
            CircleOptions()
                .center(latLng)
                .strokeColor(color)
                .strokeWidth(5f)
                .fillColor(color)
                .radius(150.0)
        )
    }


    private fun onLocationSelected(selectedLocation: SelectedLocation) {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence


        _viewModel.latitude.value = selectedLocation.latitude
        _viewModel.longitude.value = selectedLocation.longitude
        _viewModel.reminderSelectedLocationStr.value = selectedLocation.poi?.name
        _viewModel.selectedPOI.value = selectedLocation.poi

        findNavController().navigateUp()
    }


    private fun setMapStyle(map: GoogleMap) {

        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun startUsingMap() {

        Toast.makeText(
            requireContext(),
            "Select a point of interest to create a reminder!",
            Toast.LENGTH_SHORT
        ).show()

        map.setOnPoiClickListener { poi ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title("Location")
                    .snippet(poi.name)
            ).showInfoWindow()

            addingCircle(poi.latLng)

            selectedLocation.longitude = poi.latLng.longitude
            selectedLocation.latitude = poi.latLng.latitude
            selectedLocation.poi = poi
        }

        map.setOnMapLongClickListener { latLng ->
            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Location")
            ).showInfoWindow()

            addingCircle(latLng, Color.argb(65, 0, 0, 255))

            selectedLocation.longitude = latLng.longitude
            selectedLocation.latitude = latLng.latitude
            selectedLocation.poi = PointOfInterest(latLng, null, "Location")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
