package com.speed.car.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.*
import android.location.LocationListener
import android.os.Build
import android.os.Looper
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.speed.car.R
import com.speed.car.core.BaseFragment
import com.speed.car.core.utils.observe
import com.speed.car.databinding.FragmentMainBinding
import com.speed.car.interfaces.OnGpsServiceUpdate
import com.speed.car.model.Data
import com.speed.car.notification.ChannelDetail
import com.speed.car.notification.NotificationChannelType
import com.speed.car.notification.NotificationContent
import com.speed.car.notification.NotificationRepository
import com.speed.car.services.GpsServices
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(), LocationListener,
    OnMapReadyCallback {
    private val defaultZoom = 20.0f
    private var onGpsServiceUpdate: OnGpsServiceUpdate? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager
    private lateinit var sharedPreferences: SharedPreferences
    private val defaultLocation = LatLng(16.0668632, 108.2112561)
    private var locationPermissionGranted = false

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    private lateinit var locationCallback: LocationCallback

    private val notificationRepository: NotificationRepository by inject()

    companion object {
        lateinit var data: Data
    }

    override val viewModel: MainViewModel by viewModel()

    override fun getViewBinding(): FragmentMainBinding = FragmentMainBinding.inflate(layoutInflater)
    override fun observeViewModel() {
        observe(viewModel.navigateToHistory) {
            findNavController().navigate(R.id.action_mainFragment_to_historyFragment)
        }
    }

    override fun viewBinding() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        initMap()
        observers()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        data = Data(onGpsServiceUpdate)
        mLocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                markCurrentLocation(locationResult)
            }
        }
        onGpsServiceUpdate = object : OnGpsServiceUpdate {
            override fun update() {
                Log.d("xxx", "update: ")
                var maxSpeedTemp = data.maxSpeed
                var distanceTemp = data.distance
                var averageTemp: Double = if (sharedPreferences.getBoolean("auto_average", false)) {
                    data.averageSpeedMotion
                } else {
                    data.averageSpeed
                }

                val speedUnits: String
                val distanceUnits: String
                if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                    maxSpeedTemp *= 0.62137119
                    distanceTemp = distanceTemp / 1000.0 * 0.62137119
                    averageTemp *= 0.62137119
                    speedUnits = "mi/h"
                    distanceUnits = "mi"
                } else {
                    speedUnits = "km/h"
                    if (distanceTemp <= 1000.0) {
                        distanceUnits = "m"
                    } else {
                        distanceTemp /= 1000.0
                        distanceUnits = "km"
                    }
                }

                var s = SpannableString(String.format("%.0f %s", maxSpeedTemp, speedUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - speedUnits.length - 1, s.length, 0)
                // maxSpeed.setText(s)
                Toast.makeText(activity, "maxSpeed $s", Toast.LENGTH_SHORT).show()
                s = SpannableString(String.format("%.0f %s", averageTemp, speedUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - speedUnits.length - 1, s.length, 0)
                // averageSpeed.setText(s)
                Toast.makeText(activity, s.toString(), Toast.LENGTH_SHORT).show()
                s = SpannableString(String.format("%.3f %s", distanceTemp, distanceUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - distanceUnits.length - 1, s.length, 0)
                // distance.setText(s)
                Toast.makeText(activity, s.toString(), Toast.LENGTH_SHORT).show()
            }

        }
        data = Data(onGpsServiceUpdate)
    }

    override fun onStart() {
        super.onStart()
        onGrantPermissionNeeded()
    }

    override fun onResume() {
        super.onResume()
        if (mLocationManager.allProviders.indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, this)
        } else {
            Log.w(
                "MainActivity",
                "No GPS location provider found. GPS data display will not be available."
            );
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog()
        }
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val currentLocationRequest = LocationRequest()
        currentLocationRequest.setInterval(500)
            .setFastestInterval(0)
            .setMaxWaitTime(0)
            .setSmallestDisplacement(0F).priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient.requestLocationUpdates(
            currentLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().stopService(Intent(activity, GpsServices::class.java))
    }

    override fun onLocationChanged(location: Location) {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this.context, Locale.getDefault())

        addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        viewModel.onLocationChangeSpeed(location)
        Log.d("xxx","address line ${addresses[0].getAddressLine(0)}")
    }

    private fun onGrantPermissionNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.values.any { it == false }) {
                Toast.makeText(activity, "Permission request failed", Toast.LENGTH_LONG).show()
            }
        }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        getDeviceLocation()
    }

    private fun initMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun observers() {
        viewModel.currentSpeed.observe(viewLifecycleOwner) {
            binding.viewSpeed.speedTo(speed = it.first)
            Toast.makeText(activity, "current speed ${it.first}", Toast.LENGTH_SHORT).show()
        }
        viewModel.isOverSpeedLimit.observe(viewLifecycleOwner) {
            if (it.first) {
                val channelDetail = ChannelDetail(NotificationChannelType.SPEED_CAR, NotificationManager.IMPORTANCE_MAX)
                val notificationContent =
                    NotificationContent(1234, "Speed Warning", "Your current speed is ${it.second}")
                notificationRepository.sendNotification(channelDetail, notificationContent)
            }
        }
    }

    private fun markCurrentLocation(locationResult: LocationResult) {
        val lastLocation = locationResult.lastLocation
        val addresses: List<Address>
        val geocoder = Geocoder(this.context, Locale.getDefault())

        addresses = geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1)
        viewModel.checkSpeedLimit(addresses[0].thoroughfare)
        for (location in locationResult.locations) {
            with(location) {
                val current = LatLng(this.latitude, this.longitude)
                Log.d("xxx", "$location")
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        current, defaultZoom
                    )
                )
            }
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        Log.d("xxx", "getDeviceLocation: ")
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), 10.0f
                                )
                            )
                        }
                    } else {
                        Log.d("xxx", "Current location is null. Using defaults.")
                        Log.e("xxx", "Exception: %s", task.exception)
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, 10.0f)
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun showGpsDisabledDialog() {
        startActivity(Intent("android.settings.LOCATION_SOURCE_SETTINGS"))
    }
}