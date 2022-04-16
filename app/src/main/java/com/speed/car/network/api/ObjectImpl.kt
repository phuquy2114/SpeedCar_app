package com.speed.car.network.api

import com.google.android.gms.common.api.Response
import com.speed.car.network.api.entity.ObjectRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ObjectImpl(
    private val api: BkpApi
) : ObjectRepository {
    override suspend fun callApi(body: ObjectRequest): Flow<retrofit2.Response<ObjectRequest>> =
        flow {
            emit(api.callApi(body = body))
        }

}
