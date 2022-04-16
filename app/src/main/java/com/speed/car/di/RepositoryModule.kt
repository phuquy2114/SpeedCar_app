package com.speed.car.di

import com.speed.car.data.RoomRepository
import com.speed.car.notification.NotificationRepository
import com.speed.car.notification.NotificationRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single {
        RoomRepository.getDatabase(androidContext())
    }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
}


