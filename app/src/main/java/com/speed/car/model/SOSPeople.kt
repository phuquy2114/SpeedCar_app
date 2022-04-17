package com.speed.car.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SOSPeople(
    var id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("address") val address: String = "",
    @PropertyName("image") val image: String = "",
    @PropertyName("phone") val phoneNumber: String = "",
    @PropertyName("lng") val lng: Double = 0.0,
    @PropertyName("lat") val lat: Double = 0.0
) : Parcelable