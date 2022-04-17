package com.speed.car.core

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.speed.car.BuildConfig
import com.speed.car.R
import com.speed.car.core.utils.observe
import com.speed.car.model.Permission
import com.speed.car.ui.common.CommonDialogFragment
import org.koin.core.parameter.DefinitionParameters
import org.koin.core.parameter.parametersOf


abstract class BaseFragment<T : BaseViewModel, B : ViewDataBinding> : Fragment() {
    protected abstract val viewModel: T
    protected lateinit var binding: B

    abstract fun getViewBinding(): B

    open fun initialize() {}
    open fun observeViewModel() {}
    open fun events() {}
    open fun onPermissionCallBack(permission: Permission, isGranted: Boolean = false) {}
    abstract fun viewBinding()

    private val accessLocalPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            onPermissionCallBack(
                Permission.ACCESS_FINE_LOCATION,
                requireContext().isGrantedLocationPermission()
            )
        }
    private val gotoSettingIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onPermissionCallBack(Permission.ACTION_SETTINGS)
        }
    private var mView: View? = null
    protected val savedState: DefinitionParameters = parametersOf(activity, arguments)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding().apply {
            lifecycleOwner = this@BaseFragment
        }
        mView = binding.root
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        observeBase()
        observeViewModel()
        viewBinding()
        events()
    }

    private fun observeBase() {
        observe(viewModel.requestPermission, this::handleRequestPermission)
    }

    private fun handleRequestPermission(permission: Permission) {
        when (permission) {
            Permission.LOCATION_ACCESS_COARSE -> handleRequestLocation()
            Permission.ACCESS_FINE_LOCATION -> handleRequestAccessFineLocation()
            else -> {
            }
        }
    }

    private fun handleRequestLocation() {
        when (isGrantedLocationPermission()) {
            true -> onPermissionCallBack(Permission.LOCATION_ACCESS_COARSE)
            else -> showRequestLocationDialog { showApplicationDetailsSettings() }
        }
    }

    private fun handleRequestAccessFineLocation() {
        accessLocalPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun showApplicationDetailsSettings() {
        val settingIntent = Intent(Permission.ACTION_SETTINGS.value).apply {
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        }
        gotoSettingIntent.launch(settingIntent)
    }
}

fun Context.isPermissionGranted(permission: Permission): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission.value
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.isGrantedLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        requireContext(),
        Permission.LOCATION_ACCESS_COARSE.value
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.isGrantedLocationPermission(): Boolean {
    return this.isPermissionGranted(Permission.LOCATION_ACCESS_COARSE) &&
            this.isPermissionGranted(Permission.ACCESS_FINE_LOCATION)
}

fun Fragment.showRequestLocationDialog(callback: () -> Unit) {
    CommonDialogFragment.Builder(requireContext())
        .setContent(R.string.dialog_location_permission_content)
        .setPositiveButton(R.string.dialog_location_permission_go_button) {
            callback.invoke()
        }
        .setNegativeButton(R.string.dialog_location_permission_cancel_button) {
        }
        .show()
}