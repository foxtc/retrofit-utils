package com.ibrahimelgales.retrofitutils

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class ApiException(
    private val throwable: Throwable? = null,
    private val statusCode: Int? = null,
    private var message: String? = null
) {

    private val type: ExceptionType
    var messageId: Int? = null

    fun getExceptionType() = type

    fun getMessage() = message

    init {
        type = getTypeFromThrowable()
        if (message == null) {
            message = when (type) {
                ExceptionType.SERVER_DOWN -> "SERVER_DOWN"
                ExceptionType.TIME_OUT -> "TIME_OUT"
                ExceptionType.UNAUTHORIZED -> "UNAUTHORIZED"
                ExceptionType.UNKNOWN -> "UNKNOWN"
                ExceptionType.HTTP -> "HTTP"
                ExceptionType.NETWORK -> "There is no internet currently, please check your internet"
                else -> "UNKNOWN ERROR !"
            }
        }

    }


    private fun getTypeFromThrowable(): ExceptionType {
        return when (throwable) {
            is HttpException, null -> {
                when (statusCode) {
                    500, 502 -> ExceptionType.SERVER_DOWN
                    408 -> ExceptionType.TIME_OUT
                    401 -> ExceptionType.UNAUTHORIZED
                    else -> ExceptionType.UNKNOWN
                }
            }
            is TimeoutException, is SocketTimeoutException -> ExceptionType.TIME_OUT
            is ConnectException -> ExceptionType.SERVER_DOWN
            is IOException, is UnknownHostException -> ExceptionType.NETWORK
            else -> ExceptionType.UNKNOWN
        }
    }

    public enum class ExceptionType {
        HTTP, //Status is not 200,
        NETWORK, //Internet Error
        UNKNOWN,
        SERVER_DOWN, //Server didn't respond properly
        TIME_OUT,
        UNAUTHORIZED
    }
}