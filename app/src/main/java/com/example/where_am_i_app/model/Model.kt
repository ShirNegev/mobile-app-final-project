package com.example.where_am_i_app.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.where_am_i_app.model.networking.AlertsClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class Model private constructor(){
    enum class LoadingState {
        LOADING,
        LOADED
    }

    private var executor = Executors.newSingleThreadExecutor()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    val alerts: MutableLiveData<Alerts> = MutableLiveData()

    companion object {
        val shared = Model()
    }

    fun getAlerts() {
        executor.execute {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val endDate = LocalDate.now() // Today
                val startDate = endDate.minusDays(7) // 7 days ago

                val request = AlertsClient.alertsApiClient.getAlerts()
                val response = request.execute()

                if (response.isSuccessful) {
                    val alerts = response.body()
                    Log.e("TAG", "Fetched alerts! ${alerts?.alerts?.get(0)}")
                    Log.e("TAG", "Fetched alerts!.. with total number of alerts ${alerts?.alerts?.size ?: 0}")
                    this.alerts.postValue(alerts)
                } else {
                    Log.e("TAG", "Failed to fetch alerts! ${response.toString()}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Failed to fetch alerts! with exception ${e}")
            }
        }
    }
}