package com.speed.car.firestore

import com.speed.car.model.SOSPeople
import com.speed.car.model.SpeedAddress
import kotlinx.coroutines.flow.Flow

interface FirestoreRepository {
    suspend fun getSOSPeopleByAddress(address: String): Flow<List<SOSPeople>?>
    suspend fun getSOSPeople(): Flow<List<SOSPeople>?>
    suspend fun getSpeedAddressByAddress(address: String): Flow<SpeedAddress?>
}
