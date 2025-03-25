package com.example.where_am_i_app.model

import android.util.Log
import com.example.where_am_i_app.model.networking.AlertsClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class Model private constructor(){

    private var executor = Executors.newSingleThreadExecutor()

    fun getAlerts() {
        executor.execute {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val endDate = LocalDate.now() // Today
                val startDate = endDate.minusDays(7) // 7 days ago

                val request = AlertsClient.alertsApiClient.getAlerts(startDate.format(formatter), endDate.format(formatter))
                val response = request.execute()

                if (response.isSuccessful) {
                    val alerts = response.body()
                    Log.e("TAG", "Fetched alerts!.. with total number of alerts ${alerts?.alerts?.size ?: 0}")
                } else {
                    Log.e("TAG", "Failed to fetch alerts!")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Failed to fetch movies! with excpetion ${e}")
            }
        }
    }

}