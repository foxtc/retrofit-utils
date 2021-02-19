package com.ibrahimelgales.retrofitutils


data class ApiResponseCallbacks<T> (
    val onResult: (response: T?) -> Unit,
    val onError: (error: ApiException) -> Unit,
    val paging: ((hasNextPage: Boolean) -> Unit) ?= null
)