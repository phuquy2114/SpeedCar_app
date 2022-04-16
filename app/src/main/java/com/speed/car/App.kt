package com.speed.car

import android.app.Application
import com.google.firebase.FirebaseApp
import com.speed.car.di.fireStoreModule
import com.speed.car.di.repositoryModule
import com.speed.car.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        koinConfig()
    }

    private fun koinConfig() = startKoin {
        androidContext(this@App)
        modules(
            viewModelModule,
            repositoryModule,
            fireStoreModule,
        )
    }
}