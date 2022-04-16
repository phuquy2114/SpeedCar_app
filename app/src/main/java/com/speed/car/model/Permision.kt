package com.speed.car.model

import android.Manifest
import android.provider.Settings


enum class Permission(val value: String) {
    LOCATION_ACCESS_COARSE(Manifest.permission.ACCESS_COARSE_LOCATION),
    ACCESS_FINE_LOCATION(Manifest.permission.ACCESS_FINE_LOCATION),
    ACTION_SETTINGS(Settings.ACTION_APPLICATION_DETAILS_SETTINGS),
}
