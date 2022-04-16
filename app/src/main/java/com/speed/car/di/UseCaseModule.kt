package com.speed.car.di

import com.speed.car.firestore.DataUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { DataUseCase(get()) }
}