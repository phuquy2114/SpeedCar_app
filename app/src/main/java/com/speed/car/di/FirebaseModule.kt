package com.speed.car.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.dsl.module

val fireStoreModule = module {
    single { provideFireStore() }
}

internal fun provideFireStore(): FirebaseFirestore {
    return Firebase.firestore
}