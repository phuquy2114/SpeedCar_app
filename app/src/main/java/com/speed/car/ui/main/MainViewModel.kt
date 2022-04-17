package com.speed.car.ui.main

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.speed.car.core.BaseViewModel
import com.speed.car.firestore.FirestoreRepository
import com.speed.car.model.SOSPeople
import com.speed.car.model.SpeedAddress
import com.speed.car.utils.SharedPreferencesH
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainViewModel(
    private val sharedPreferences: SharedPreferencesH,
    private val fireStoreRepository: FirestoreRepository,
) : BaseViewModel() {
    val currentSpeed = MutableLiveData<Pair<Float, String>>()
    val maxSpeed = MutableLiveData<Pair<Float, String>>()
    val distance = MutableLiveData<Pair<Float, String>>()
    val average = MutableLiveData<Pair<Float, String>>()
    val currentAcc = MutableLiveData<Pair<Float, String>>()
    private var currentLocation: Location? = null
    val speedLimitCurrent = MutableLiveData<Int?>()
    val voiceRate = MutableLiveData<Boolean>(false)
    val speedAddress = MutableLiveData<SpeedAddress>()

    val isOverSpeedLimit: LiveData<Pair<Boolean, Float>> = currentSpeed.map {
        val limit = speedLimitCurrent.value ?: return@map false to it.first
        (it.first > limit) to it.first
    }

    val isEnableSOS = MutableLiveData(false)
    val isMotorMode = MutableLiveData(false)
    val isVisibleLimit = speedLimitCurrent.map {
        it != null
    }
    val speedLimitCurrentStr = speedLimitCurrent.map {
        it.toString().ifEmpty { "--" }
    }

    val maxSpeedView = maxSpeed.map {
        it.second.ifEmpty { "00" }
    }


    val distanceView = distance.map {
        it.second.ifEmpty { "00" }
    }


    val avaView = average.map {
        it.second.ifEmpty { "00" }
    }


    val accView = currentAcc.map {
        it.second.ifEmpty { "00" }
    }

    var currentWayName: String? = null

    var markers: List<Marker?> = listOf()

    val sosPeople = MutableLiveData<List<SOSPeople>>()

    init {
        launchCoroutine {
            Log.d("xxx", "init : ")

            sharedPreferences.getBoolean("enable-sos").let {
                isEnableSOS.postValue(it)
            }

            fireStoreRepository.getSOSPeople().collectLatest {
                Log.d("xxx", "sos: $it")
                sosPeople.postValue(it ?: listOf())
            }
        }
    }

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
            val speedMotor = if (isMotorMode.value == true) 40f else 30f
            if (speed.toFloat() > speedLimitCurrent.value?.toFloat() ?: speedMotor) {
                voiceRate.postValue(true)
            } else {
                voiceRate.postValue(false)
            }
        }
    }

    fun checkSpeedLimit(wayName: String) {
        Log.d("xxx", "checkSpeedLimit: $wayName")
        viewModelScope.launch {
            if (wayName != currentWayName) {
                currentWayName = wayName
            }
            fireStoreRepository.getSpeedAddressByAddress(address = currentWayName ?: "").collectLatest {
                Log.d("xxx", "response :$it ")
                speedLimitCurrent.postValue(it?.maxSpeed)
                speedAddress.postValue(it)
            }
        }
    }

    fun setEnableSoS(isEnable: Boolean) {
        isEnableSOS.postValue(isEnable)
        sharedPreferences.putBoolean("enable-sos", isEnable)
    }
    fun setTurnVehicleSpeed(isEnable: Boolean) {
        isMotorMode.postValue(isEnable)
    }
}