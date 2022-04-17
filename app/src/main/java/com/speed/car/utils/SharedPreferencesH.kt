package com.speed.car.utils

import android.content.Context
import androidx.preference.PreferenceManager

class SharedPreferencesH constructor(context: Context) {
    private val instance = PreferenceManager.getDefaultSharedPreferences(context)
    fun getBoolean(key: String) = instance.getBoolean(key, false)
    fun putBoolean(key: String, value: Boolean) = instance.edit().putBoolean(key, value).apply()
}