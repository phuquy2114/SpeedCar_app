package com.speed.car.di

import com.speed.car.ui.history.HistoryViewModel
import com.speed.car.ui.main.MainViewModel
import com.speed.car.utils.SharedPreferencesH
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
	viewModel { MainViewModel(get()) }
	factory { SharedPreferencesH(androidApplication()) }
	viewModel { MainViewModel(get()) }
	viewModel { HistoryViewModel() }
}
