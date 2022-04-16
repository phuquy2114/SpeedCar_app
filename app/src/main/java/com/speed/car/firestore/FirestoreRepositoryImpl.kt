package com.speed.car.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.speed.car.model.SOSPeople
import com.speed.car.model.SpeedAddress
import com.speed.car.utils.toListOrEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


class FirestoreRepositoryImpl(
    private val fireStore: FirebaseFirestore,
) : FirestoreRepository {

    override suspend fun getSOSPeopleByAddress(address: String): List<SOSPeople> {
        return fireStore.collection("SOSPeople")
            .whereEqualTo("address", address)
            .get()
            .await()
            .toListOrEmpty(SOSPeople::class.java) {
                id = it.id
            }

    }

    override suspend fun getSpeedAddressByAddress(address: String): Flow<SpeedAddress?> = flow {
        fireStore.collection("SpeedAddress")
            .whereEqualTo("address", address)
            .get()
            .await()
            .toListOrEmpty(SpeedAddress::class.java) {
                id = it.id
            }.also {
                emit(it.firstOrNull())
            }

    }.catch { emit(null) }
}
