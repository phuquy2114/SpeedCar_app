package com.speed.car.firestore

import com.speed.car.model.SpeedAddress

class DataUseCase(
    private val fireStoreRepository: FirestoreRepository,
) {
    suspend fun getSpeedAddressByAddress(address: String): SpeedAddress? {
        return fireStoreRepository.getSpeedAddressByAddress(address)
    }

}