package com.speed.car.core

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.speed.car.utils.SingleEvent
import com.bitkey.workhub.utils.SingleLiveEvent
import com.speed.car.model.Permission
import com.speed.car.network.ErrorResponse
import com.speed.car.network.ResponseWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent

abstract class BaseViewModel : ViewModel() {

    private val mApplication: Application by KoinJavaComponent.inject(Application::class.java)

    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        }
    val navigationEvent: MutableLiveData<SingleEvent<NavController.() -> Any>> = MutableLiveData()
    val requestPermission = SingleLiveEvent<Permission>()
    val responseErrorWrapperEvent = MutableLiveData<ResponseWrapper<*>>()
    fun requestPermission(permission: Permission) {
        requestPermission.value = permission
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T> Flow<ResponseWrapper<T>>.subscribe(
        onSuccess: ((T) -> Unit)? = null,
        onError: ((Int, ErrorResponse?) -> Unit)? = null
    ) {
        collect { response ->
            withContext(Dispatchers.Main) {
                when (response) {
                    is ResponseWrapper.Success -> onSuccess?.invoke(response.value)
                    is ResponseWrapper.SuccessEmpty -> onSuccess?.invoke(response.value as T)
                    is ResponseWrapper.GenericError -> onError?.handleLogError(
                        response.code,
                        response.error
                    )
                    is ResponseWrapper.NetworkError -> onError?.handleLogError(
                        response.code,
                        null
                    )
                }
                if (response !is ResponseWrapper.Success) {
                    responseErrorWrapperEvent.postValue(response)
                }
            }
        }
    }

    private fun ((Int, ErrorResponse?) -> Unit)?.handleLogError(
        code: Int = -1,
        error: ErrorResponse? = null
    ) {
        this?.invoke(code, error)
    }
}
