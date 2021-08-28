package com.ibrahimelgales.retrofitutils

import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

private const val READ_TIME_OUT_CONNECTION = 1
private const val WRITE_TIME_OUT_CONNECTION = 1
private const val TIME_OUT_CONNECTION = 1
private val TINE_UNIT_FOR_CONNECTION = TimeUnit.MINUTES

object RetroClient {
    fun provideRetrofit(baseUrl: String, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(READ_TIME_OUT_CONNECTION.toLong(), TINE_UNIT_FOR_CONNECTION)
            .writeTimeout(WRITE_TIME_OUT_CONNECTION.toLong(), TINE_UNIT_FOR_CONNECTION)
            .connectTimeout(TIME_OUT_CONNECTION.toLong(), TINE_UNIT_FOR_CONNECTION)

            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level =
                    HttpLoggingInterceptor.Level.BODY
            }).build()
    }
}


abstract class AuthInterceptor(
    private val language: String? = null,
    private val token: Pair<RetrofitTokenType, String?>? = null,
    val responseStatusCode: ((Int) -> Unit)? = null
) : Interceptor {
    open fun myRequest(request: Request) {}
    open fun myResponse(response: Response) {}
    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        val request = original.newBuilder().apply {
            header("Content-Type", "application/json")
            language?.let {
                if (it.trim().isNotEmpty())
                    header("Accept-Language", language)
            }
            token?.let { pair ->
                pair.second?.let {
                    if (it.trim().isNotEmpty())
                        header("Authorization", "${pair.first}$it")
                }
            }
        }.build()


        val map = HashMap<String, Any>()
        map["request header"] = Gson().toJson(request.headers())
        map["request body"] = Gson().toJson(request.body())
        map["method"] = Gson().toJson(request.method())
        map["query"] = Gson().toJson(request.url().query())
        //map.put("path",req.url.encodedPath?: "")
        val response = chain.proceed(request)
        map["response"] = responseToString(response, request.body()?.contentType())
        map["code"] = response.code()
        map["message"] = response.message()

        myRequest(request)
        myResponse(response)

        responseStatusCode?.invoke(response.code())
        return response
    }

    private fun responseToString(response: Response, contentType: MediaType?): String {
        val UTF8 = Charset.forName("UTF-8")
        val buffer: Buffer
        val source = response.body()?.source()
        source?.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
        if (source != null) {
            buffer = source.buffer()
            var charset: Charset? = UTF8
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            val responseString = StringBuilder()
            if (response.body()?.contentLength() ?: 0 != 0L) {
                responseString.append(buffer.clone().readString(charset!!))
            }
            return responseString.toString()
        }
        return ""
    }
}

enum class RetrofitTokenType(val typeName: String) {
    BEARER("Bearer "),
    JWT("JWT ")
}