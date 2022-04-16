package com.speed.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.speed.car.interfaces.OnGpsServiceUpdate
import com.speed.car.model.Data
import com.speed.car.services.GpsServices
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener, GpsStatus.Listener, OnMapReadyCallback {

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

    companion object {
        lateinit var data: Data
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        data = Data(onGpsServiceUpdate)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

                s = SpannableString(String.format("%.0f %s", averageTemp, speedUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - speedUnits.length - 1, s.length, 0)
                // averageSpeed.setText(s)

                s = SpannableString(String.format("%.3f %s", distanceTemp, distanceUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - distanceUnits.length - 1, s.length, 0)
                // distance.setText(s)
            }

        }

    }

    override fun onStart() {
        super.onStart()
        onGrantPermissionNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, GpsServices::class.java))
    }

    override fun onLocationChanged(location: Location) {
        Log.d("xxx", "onLocationChanged: ")
        if (location.hasAccuracy()) {
            var acc: Double = location.accuracy.toDouble()
            val units: String
            if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                units = "ft"
                acc *= 3.28084
            } else {
                units = "m"
            }
            val s = SpannableString(String.format("%.0f %s", acc, units))
            s.setSpan(RelativeSizeSpan(0.75f), s.length - units.length - 1, s.length, 0)

        }

        if (location.hasSpeed()) {
            var speed: Double = location.speed * 3.6
            val units: String
            if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
                speed *= 0.62137119
                units = "mi/h"
            } else {
                units = "km/h"
            }
            val s =
                SpannableString(java.lang.String.format(Locale.ENGLISH, "%.0f %s", speed, units))
            s.setSpan(RelativeSizeSpan(0.25f), s.length - units.length - 1, s.length, 0)
            //currentSpeed.setText(s)
            Log.d("xxx", speed.toString())
            Log.d("xxx", s.toString())
        }
    }

    override fun onGpsStatusChanged(event: Int) {
        when (event) {
            GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                Log.d("xxx", "onGpsStatusChanged: ")
            }
            GpsStatus.GPS_EVENT_STOPPED -> if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("xxx", "onGpsStatusChanged: ")
            }
            GpsStatus.GPS_EVENT_FIRST_FIX -> {
                Log.d("xxx", "onGpsStatusChanged: ")
            }
        }
    }


    private fun onGrantPermissionNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
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
                    this,
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
                //TODO
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val current = LatLng(16.0668632, 108.2112561)
        mMap.addMarker(
            MarkerOptions()
                .position(current)
                .title("Marker current")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current))

        getDeviceLocation()
    }

    private fun initViews() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun markCurrentLocation(location: Location) {
        with(location) {
            val current = LatLng(this.longitude, this.latitude)
            Log.d("xxx", "$location")
            mMap.addMarker(
                MarkerOptions()
                    .position(current)
                    .title("Marker current")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current))
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), 10.0f))
                        }
                    } else {
                        Log.d("xxx", "Current location is null. Using defaults.")
                        Log.e("xxx", "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, 10.0f))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}