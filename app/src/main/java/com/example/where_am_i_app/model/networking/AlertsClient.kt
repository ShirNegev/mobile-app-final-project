package com.example.where_am_i_app.model.networking

import com.example.where_am_i_app.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AlertsClient {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .build()
    }

    val alertsApiClient: AlertsAPI by lazy {
        val retrofitClient = Retrofit.Builder()
            .baseUrl(BuildConfig.ALERTS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitClient.create(AlertsAPI::class.java)
    }
}