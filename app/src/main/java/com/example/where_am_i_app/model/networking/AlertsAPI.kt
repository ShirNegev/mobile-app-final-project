package com.example.where_am_i_app.model.networking

import com.example.where_am_i_app.model.Alerts
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface AlertsAPI {
    @Headers("accept: application/json")
    @GET("earthquakes/feed/v1.0/summary/2.5_day.geojson")
    fun getAlerts(
    ): Call<Alerts>
}