package com.speed.car.di

import com.speed.car.ui.history.HistoryViewModel
import com.speed.car.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
}
