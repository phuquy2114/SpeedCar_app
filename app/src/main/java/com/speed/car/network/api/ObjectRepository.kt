package com.speed.car.network.api

import com.speed.car.network.api.entity.ObjectRequest
import kotlinx.coroutines.flow.Flow
import retrofit2.Response


interface ObjectRepository {
    suspend fun callApi(body: ObjectRequest): Flow<Response<ObjectRequest>>
}
