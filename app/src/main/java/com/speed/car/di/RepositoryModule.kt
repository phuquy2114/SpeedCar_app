package com.speed.car.di

import com.speed.car.data.RoomRepository
import com.speed.car.domain.usecase.HistoryUseCase
import com.speed.car.firestore.FirestoreRepository
import com.speed.car.firestore.FirestoreRepositoryImpl
import com.speed.car.notification.NotificationRepository
import com.speed.car.notification.NotificationRepositoryImpl
import com.speed.car.utils.SharedPreferencesH
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single {
        RoomRepository.getDatabase(androidContext())
    }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<FirestoreRepository> { FirestoreRepositoryImpl(get()) }
    single { SharedPreferencesH(androidApplication()) }
    single { HistoryUseCase(get()) }

}


