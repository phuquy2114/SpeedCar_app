package com.speed.car.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpeedAddress(
    var id: String = "",
    @PropertyName("address") val address: String = "",
    @PropertyName("maxSpeed") val maxSpeed: String = "",
) : Parcelable