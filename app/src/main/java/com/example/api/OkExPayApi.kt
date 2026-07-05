package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OkExPayResponse(
    val code: Int,
    val msg: String,
    val data: OkExPayData? = null
)

@JsonClass(generateAdapter = true)
data class OkExPayData(
    val url: String? = null,
    val transaction_Id: String? = null
)

@JsonClass(generateAdapter = true)
data class OkExPayQueryResponse(
    val code: Int,
    val msg: String,
    val data: OkExPayQueryData? = null
)

@JsonClass(generateAdapter = true)
data class OkExPayQueryData(
    val transaction_Id: String? = null,
    val mchId: Int? = null,
    val out_trade_no: String? = null,
    val status: Int? = null, // 0 pending, 1 payment success, 2 payment failed
    val money: Double? = null,
    val currency: String? = null
)

interface OkExPayApiService {
    @FormUrlEncoded
    @POST("v1/Collect")
    suspend fun collect(
        @FieldMap fields: Map<String, String>
    ): OkExPayResponse

    @FormUrlEncoded
    @POST("v1/Query/Collect")
    suspend fun queryCollect(
        @FieldMap fields: Map<String, String>
    ): OkExPayQueryResponse

    @FormUrlEncoded
    @POST("v1/Query/Payout")
    suspend fun queryPayout(
        @FieldMap fields: Map<String, String>
    ): OkExPayQueryResponse
}

object OkExPayClient {
    private const val BASE_URL = "https://sandbox.okexpay.dev/"
    const val DEFAULT_MCH_ID = "1000"
    const val DEFAULT_KEY = "4035fcd2d720e1b06ea455bdde411012"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: OkExPayApiService by lazy {
        getService(BASE_URL)
    }

    fun getService(baseUrl: String): OkExPayApiService {
        val finalUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(finalUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(OkExPayApiService::class.java)
    }

    // Sign generator helper
    fun generateSign(params: Map<String, String>, key: String): String {
        // Sort parameters lexicographically
        val sortedParams = params.filter { it.value.isNotEmpty() && it.key != "sign" }
            .toSortedMap()
        val stringA = sortedParams.map { "${it.key}=${it.value.trim()}" }.joinToString("&")
        val stringSignTemp = "$stringA&key=$key"
        return md5(stringSignTemp).lowercase()
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
