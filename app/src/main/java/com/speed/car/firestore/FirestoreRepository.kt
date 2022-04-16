package com.speed.car.firestore

import com.speed.car.model.SOSPeople
import com.speed.car.model.SpeedAddress

interface FirestoreRepository {
    suspend fun getSOSPeopleByAddress(address: String): List<SOSPeople>?
    suspend fun getSpeedAddressByAddress(address: String): SpeedAddress?
}
