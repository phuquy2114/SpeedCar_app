package com.speed.car.ui.main

import android.util.Log
import com.bitkey.workhub.utils.SingleLiveEvent
import com.speed.car.core.BaseViewModel
import com.speed.car.firestore.FirestoreRepository
import kotlinx.coroutines.flow.collectLatest


class MainViewModel(
    private val fireStoreRepository: FirestoreRepository,
) : BaseViewModel() {
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