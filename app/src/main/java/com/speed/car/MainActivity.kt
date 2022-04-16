package com.speed.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.github.anastr.speedviewlib.AwesomeSpeedometer
import com.google.android.gms.location.*
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


class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private val defaultZoom = 16.0f

    private var onGpsServiceUpdate: OnGpsServiceUpdate? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager
    private lateinit var sharedPreferences: SharedPreferences
    private val defaultLocation = LatLng(16.0668632, 108.2112561)
    private var locationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    private lateinit var locationCallback: LocationCallback
    private lateinit var speedometer: AwesomeSpeedometer

    companion object {
        lateinit var data: Data
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                Toast.makeText(this@MainActivity, "maxSpeed $s", Toast.LENGTH_SHORT).show()
                s = SpannableString(String.format("%.0f %s", averageTemp, speedUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - speedUnits.length - 1, s.length, 0)
                // averageSpeed.setText(s)
                Toast.makeText(this@MainActivity, s.toString(), Toast.LENGTH_SHORT).show()
                s = SpannableString(String.format("%.3f %s", distanceTemp, distanceUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - distanceUnits.length - 1, s.length, 0)
                // distance.setText(s)
                Toast.makeText(this@MainActivity, s.toString(), Toast.LENGTH_SHORT).show()
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
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
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
            )
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog()
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
            speedometer.speedTo(speed = speed.toFloat())
            Toast.makeText(this, "current speed $s", Toast.LENGTH_SHORT).show()
            Log.d("xxx", speed.toString())
            Log.d("xxx", s.toString())
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
                Toast.makeText(this, "Permission request failed", Toast.LENGTH_LONG).show()
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val current = LatLng(16.0668632, 108.2134448)
        mMap.addMarker(
            MarkerOptions()
                .position(current)
                .title("Marker current")
        )
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                current, 15.0f
            )
        )

        getDeviceLocation()
    }

    private fun initViews() {
        speedometer = findViewById(R.id.viewSpeed)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun markCurrentLocation(locationResult: LocationResult) {
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
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        Log.d("xxx", "getDeviceLocation: ")
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
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