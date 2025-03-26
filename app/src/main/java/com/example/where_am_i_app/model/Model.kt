package com.example.where_am_i_app.model

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.where_am_i_app.model.networking.AlertsClient
import java.util.concurrent.Executors

class Model private constructor(){
    enum class LoadingState {
        LOADING,
        LOADED
    }

    private var executor = Executors.newSingleThreadExecutor()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    val alerts: MutableLiveData<List<Alert>> = MutableLiveData()

    private val cloudinaryModel = CloudinaryModel()

    companion object {
        val shared = Model()
    }

    fun getAlerts() {
        loadingState.postValue(LoadingState.LOADING)
        executor.execute {
            try {
                val request = AlertsClient.alertsApiClient.getAlerts()
                val response = request.execute()

                if (response.isSuccessful) {
                    val alerts = response.body()
                    Log.e("TAG", "Fetched alerts!.. with total number of alerts ${alerts?.alerts?.size ?: 0}")
                    val sortedAlerts = alerts?.alerts
                        ?.map { it.properties }
                        ?.sortedByDescending { it.time }
                    this.alerts.postValue(sortedAlerts)
                } else {
                    Log.e("TAG", "Failed to fetch alerts! ${response.toString()}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Failed to fetch alerts! with exception ${e}")
            }
        }
        loadingState.postValue(LoadingState.LOADED)
    }

    private fun uploadImageToCloudinary(
        bitmap: Bitmap,
        name: String,
        onSuccess: (String?) -> Unit,
        onError: (String?) -> Unit
    ) {
        cloudinaryModel.uploadImage(
            bitmap = bitmap,
            name = name,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}