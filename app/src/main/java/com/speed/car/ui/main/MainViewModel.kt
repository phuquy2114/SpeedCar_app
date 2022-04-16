package com.speed.car.ui.main

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.bitkey.workhub.utils.SingleLiveEvent
import com.speed.car.core.BaseViewModel
import com.speed.car.utils.SharedPreferencesH
import kotlinx.coroutines.launch
import com.speed.car.firestore.FirestoreRepository
import kotlinx.coroutines.flow.collectLatest


class MainViewModel(
    private val sharedPreferences: SharedPreferencesH,
    private val fireStoreRepository: FirestoreRepository,
) : BaseViewModel() {
    val currentSpeed = MutableLiveData<Pair<Float, String>>()
    val currentAcc = MutableLiveData<Pair<Float, String>>()

    private var currentLocation: Location? = null
    val speedLimitCurrent = MutableLiveData<Int?>()
    val isOverSpeedLimit: LiveData<Pair<Boolean, Float>> = currentSpeed.map {
        val limit = speedLimitCurrent.value ?: return@map false to it.first
        (it.first > limit) to it.first
    }

    val isVisibleLimit = speedLimitCurrent.map {
        it != null
    }
    val speedLimitCurrentStr = speedLimitCurrent.map {
        it.toString() ?: "--"
    }

    private var currentWayName: String? = null

    fun onLocationChangeSpeed(location: Location) {
        currentLocation = location
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
            Log.d("xxx", "onLocationChangeSpeed: $speed")
            currentSpeed.postValue(Pair(speed.toFloat(), units))
        }
    }

    fun checkSpeedLimit(wayName: String) {
        viewModelScope.launch {
            if (wayName != currentWayName) {
                currentWayName = wayName
            }
        }
    }
    val navigateToHistory = SingleLiveEvent<Boolean>()

    init {

        launchCoroutine {
            Log.d("TAGGG", "init : ")

            fireStoreRepository.getSpeedAddressByAddress("Điện Biên Phủ").collectLatest {
                Log.d("TAGGG", "response :$it ")

            }
        }
    }

    fun navigateToHistory() {
        navigateToHistory.postValue(true)
    }
}