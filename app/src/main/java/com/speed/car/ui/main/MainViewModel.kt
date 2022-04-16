package com.speed.car.ui.main

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.speed.car.core.BaseViewModel
import com.speed.car.utils.SharedPreferencesH


class MainViewModel(
    private val sharedPreferences: SharedPreferencesH
) : BaseViewModel() {
    val currentSpeed = MutableLiveData<Pair<Float, String>>()
    val currentAcc = MutableLiveData<Pair<Float, String>>()

    fun onLocationChangeSpeed(location: Location) {
        if (location.hasAccuracy()) {
            var acc: Double = location.accuracy.toDouble()
            val units: String
            if (sharedPreferences.getBoolean("miles_per_hour")) {
                units = "ft"
                acc *= 3.28084
            } else {
                units = "m"
            }
            currentAcc.postValue(Pair(acc.toFloat(), units))
        }
        if (location.hasSpeed()) {
            var speed: Double = location.speed * 3.6
            val units: String =
                if (sharedPreferences.getBoolean("miles_per_hour")) {
                    speed *= 0.62137119
                    "mi/h"
                } else {
                    "km/h"
                }
            currentSpeed.postValue(Pair(speed.toFloat(), units))
        }
    }
}