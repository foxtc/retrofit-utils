package com.ibrahimelgales.retrofitutils

import android.util.Log
import retrofit2.Response


class BaseApiProvide(
    private val customErrorsKeyName: String? = null,
    private val paginationHeaderName: String? = null
) {


    suspend fun <T> apiRequest(
        call: suspend () -> Response<T>,
        apiResponseCallbacks: ApiResponseCallbacks<T>,
        paginationHeaderName: String? = this.paginationHeaderName,
        customErrorsKeyName: String? = this.customErrorsKeyName
    ) {
        try {
            val response = call.invoke()


            if (response.success()) {
                val body = response.body()
                apiResponseCallbacks.onResult(body)

                if (paginationHeaderName != null)
                    apiResponseCallbacks.paging?.invoke(hasNextPage(paginationHeaderName, response))
            } else {
                apiResponseCallbacks.onError(
                    ApiException(
                        statusCode = response.code(),
                        message = message(customErrorsKeyName, response, response.body())
                    )
                )
                apiResponseCallbacks.paging?.invoke(false)
            }

        } catch (e: Throwable) {
            Log.e("TAG", "apiRequest: ", e)
            e.printStackTrace()
            apiResponseCallbacks.paging?.invoke(false)
            return apiResponseCallbacks.onError(ApiException(e))
        }
    }

    private fun <T> message(
        customErrorsKeyName: String?,
        response: Response<T>,
        body: T?
    ) =
        if (customErrorsKeyName != null)
            response.errorBody()?.retrofitCustomErrorResponse(customErrorsKeyName)
        else if (body is BaseResponse && !response.success())
            body.msg
        else
            response.errorBody()?.toString()

    private fun <T> hasNextPage(paginationHeaderName: String, response: Response<T>): Boolean {
        var hasNextPage = false
        val currentPage = response.headers()[paginationHeaderName]?.toIntOrNull()
        val lastPage = response.headers()[paginationHeaderName]?.toIntOrNull()
        Log.i("TAG", "hasNextPage: page >>  currentPage = $currentPage, lastPage = $lastPage")
        if (currentPage != null && lastPage != null) {
            hasNextPage = currentPage < lastPage
            Log.i("TAG", "page >> hasNextPage = $hasNextPage")
        }
        return hasNextPage
    }

    private fun <T> Response<T>.success() = body().let {
        isSuccessful && (if (it is BaseResponse) it.error.not() else true)
    }

}
