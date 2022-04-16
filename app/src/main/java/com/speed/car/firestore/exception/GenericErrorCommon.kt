package com.speed.car.firestore.exception

import com.speed.car.network.ErrorResponse
import com.speed.car.network.ResponseWrapper
import java.lang.Exception

object GenericErrorCommon {
    val organizationIdNotFound = ResponseWrapper.GenericError(
        code = 404,
        error = ErrorResponse(code = 404, "organization Id not found")
    )

    val userInfoIdNotFound = ResponseWrapper.GenericError(
        code = 404,
        error = ErrorResponse(code = 404, "User info not found")
    )

    val authenticatedInfoNotFound = ResponseWrapper.GenericError(
        error = ErrorResponse(code = 404, "authenticated Info Id not found")
    )

    val unknownException = ResponseWrapper.GenericError(
        error = ErrorResponse(code = 500, "Unknown Exception")
    )

    val jobCanceledException = ResponseWrapper.GenericError(
        error = ErrorResponse(code = 500, "Job was cancelled")
    )

    val reservationInvalidTime = ResponseWrapper.GenericError(
        code = 500,
        error = ErrorResponse(code = 500, "Invalid reservation data. Not exist time.")
    )

    val notFoundData = ResponseWrapper.GenericError(
        code = 500,
        error = ErrorResponse(code = 500, "Not found.")
    )
}
