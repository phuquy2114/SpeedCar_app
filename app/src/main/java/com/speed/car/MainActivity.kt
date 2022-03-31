package com.speed.car

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.GpsStatus
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationListener
import com.speed.car.interfaces.OnGpsServiceUpdate
import com.speed.car.model.Data
import com.speed.car.services.GpsServices
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener, GpsStatus.Listener {

    private var onGpsServiceUpdate: OnGpsServiceUpdate? = null
    private lateinit var mLocationManager: LocationManager
    private lateinit var data: Data
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        onGpsServiceUpdate = object : OnGpsServiceUpdate {
            override fun update() {
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

        data = Data(onGpsServiceUpdate)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, GpsServices::class.java))
    }

    override fun onLocationChanged(location: Location) {
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
}