package com.speed.car.network.api.entity

import com.google.gson.annotations.SerializedName

data class ObjectRequest(
    @SerializedName("field1") val field1: String,
    @SerializedName("field2") val field2: String,
    @SerializedName("field3") val field3: String,
)
