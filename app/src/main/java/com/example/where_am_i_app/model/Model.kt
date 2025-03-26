package com.example.where_am_i_app.model

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.where_am_i_app.User
import com.example.where_am_i_app.base.EmptyCallback
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
    private val firebaseModel = FirebaseModel()

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

    fun addUser(user: User, image: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addUser(user) {
            Log.e("TAG", "Uploaded user to firebase. uploading image ${image}")
            image?.let {
                uploadImageToCloudinary(
                    bitmap = image,
                    name = user.id,
                    callback = { uri ->
                        if (!uri.isNullOrBlank()) {
                            val st = user.copy(profileImageUrl = uri)
                            firebaseModel.addUser(st, callback)
                        } else {
                            Log.e("TAG", "Image upload failed, no URL returned")
                            callback()
                        }
                    },
                )
            } ?: callback()
        }
    }

    fun uploadImageToCloudinary(
        bitmap: Bitmap,
        name: String,
        callback: (String?) -> Unit
    ) {
        cloudinaryModel.uploadImage(
            bitmap = bitmap,
            name = name,
            onSuccess = callback,
            onError = { callback(null) }
        )
    }

    fun getUserById(userId: String, callback: (User?) -> Unit, errorCallback: (String?) -> Unit) {
        firebaseModel.getUserById(userId, callback, errorCallback)
    }
}