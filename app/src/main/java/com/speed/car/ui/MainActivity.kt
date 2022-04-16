package com.speed.car.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.GpsStatus
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.location.LocationListener
import com.speed.car.R
import com.speed.car.ui.main.MainFragment

import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}