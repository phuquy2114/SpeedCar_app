package com.speed.car.core

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.ViewDataBinding
import com.speed.car.R

abstract class BaseActivity<T : BaseViewModel, B : ViewDataBinding> : AppCompatActivity() {

	protected abstract val viewModel: T
	private lateinit var binding: B
	abstract fun getViewBinding(): B

	open fun initialize() {}
	open fun observeViewModel() {}

	override fun onCreate(savedInstanceState: Bundle?) {
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		super.onCreate(savedInstanceState)
		binding = getViewBinding().apply { lifecycleOwner = this@BaseActivity }
		setContentView(binding.root)
		initialize()
		observeViewModel()
		viewBinding()
		events()
	}

	abstract fun viewBinding()
	open fun events() {}

}
