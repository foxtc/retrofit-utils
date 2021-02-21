package com.ibrahimelgales.retrofitutils

import retrofit2.Retrofit

inline fun <reified T > createApiService(retrofit: Retrofit): T = retrofit.create(T::class.java)