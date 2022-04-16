package com.speed.car.network.api

import com.speed.car.network.api.entity.ObjectRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BkpApi {
    @POST("")
    suspend fun callApi(@Body body: ObjectRequest): Response<ObjectRequest>
}
