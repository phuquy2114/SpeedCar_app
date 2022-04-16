package com.speed.car.network

enum class ErrorCode(val code: Int) {
    NETWORK(511)
}

sealed class ResponseWrapper<out T> {
    data class Success<out T>(val value: T) : ResponseWrapper<T>()
    data class SuccessEmpty(val value: Any = Any()) : ResponseWrapper<Nothing>()
    data class GenericError(val code: Int = -1, val error: ErrorResponse? = null) : ResponseWrapper<Nothing>()
    data class NetworkError(val code: Int = ErrorCode.NETWORK.code) : ResponseWrapper<Nothing>()
}

data class ErrorResponse(
    val code: Int,
    val message: String
)
