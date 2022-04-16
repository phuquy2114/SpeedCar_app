package com.speed.car.ui.main

import android.util.Log
import com.bitkey.workhub.utils.SingleLiveEvent
import com.speed.car.core.BaseViewModel
import com.speed.car.firestore.DataUseCase


class MainViewModel(
    private val dataUseCase: DataUseCase,
) : BaseViewModel() {
    val navigateToHistory = SingleLiveEvent<Boolean>()

    init {
        launchCoroutine {
            val response = dataUseCase.getSpeedAddressByAddress("Điện Biên Phủ")
            Log.d("TAGGG", ":$response ")
        }
    }

    fun navigateToHistory() {
        navigateToHistory.postValue(true)
    }
}