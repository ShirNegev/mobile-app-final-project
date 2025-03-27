package com.example.where_am_i_app.model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.where_am_i_app.User
import com.example.where_am_i_app.base.EmptyCallback
import com.example.where_am_i_app.model.dao.AppLocalDb
import com.example.where_am_i_app.model.dao.AppLocalDbRepository
import com.example.where_am_i_app.model.networking.AlertsClient
import java.util.concurrent.Executors

class Model private constructor(){
    enum class LoadingState {
        LOADING,
        LOADED
    }

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val cloudinaryModel = CloudinaryModel()
    private val firebaseModel = FirebaseModel()

    private var executor = Executors.newSingleThreadExecutor()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    val alerts: MutableLiveData<List<Alert>> = MutableLiveData()
    private var mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    val userAlertReports: LiveData<List<UserAlertReport>> = database.userAlertReportDao().getAllUserAlertReports()

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

    fun addUser(user: User, callback: EmptyCallback) {
        firebaseModel.addUser(user) {}
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

    // UserAlertReports Section:

    fun refreshAllUserAlertReports() {
        loadingState.postValue(LoadingState.LOADING)
        val lastUpdated: Long = UserAlertReport.lastUpdated
        firebaseModel.getAllUserAlertReports(lastUpdated) { userAlertReports ->
            executor.execute {
                var currentTime = lastUpdated
                for (userAlertReport in userAlertReports) {
                    database.userAlertReportDao().insertAll(userAlertReport)
                    userAlertReport.lastUpdated?.let {
                        if (currentTime < it) {
                            currentTime = it
                        }
                    }
                }
                UserAlertReport.lastUpdated = currentTime
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    fun getUserAlertReportById(userAlertReportId: String, callback: (UserAlertReport?) -> Unit) {
        // Check local database for fast response
        executor.execute {
            try {
                val localUserAlertReport = database.userAlertReportDao().getUserAlertReportById(userAlertReportId)
                mainHandler.post {
                    callback(localUserAlertReport)

                    // Check from Firebase to ensure latest data
                    firebaseModel.getUserAlertReportsById(userAlertReportId) { firebaseUserAlertReport ->
                        if (firebaseUserAlertReport != null && (localUserAlertReport == null ||
                                    firebaseUserAlertReport.lastUpdated != localUserAlertReport.lastUpdated)) {
                            // Update local cache if Firebase has newer data
                            executor.execute {
                                database.userAlertReportDao().insertAll(firebaseUserAlertReport)
                                mainHandler.post {
                                    callback(firebaseUserAlertReport)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error fetching userAlertReport from local DB Room")
                mainHandler.post {
                    // Fall back to Firebase
                    firebaseModel.getUserAlertReportsById(userAlertReportId, callback)
                }
            }
        }
    }

    fun addUserAlertReport(userAlertReport: UserAlertReport, image: Bitmap?, callback: EmptyCallback) {
        if (image != null) {
            cloudinaryModel.uploadImage(
                bitmap = image,
                name = userAlertReport.id,
                onSuccess = { uri ->
                    if (!uri.isNullOrBlank()) {
                        val userAlertReportWithImage = userAlertReport.copy(reportImageUrl = uri)

                        firebaseModel.addUserAlertReport(userAlertReportWithImage) {
                            // Update local database
                            executor.execute {
                                database.userAlertReportDao().insertAll(userAlertReportWithImage)
                                mainHandler.post {
                                    callback()
                                }
                            }
                        }
                    } else {
                        // Image upload failed- save user alert report without changing URL
                        saveUserAlertReportToFirebaseAndLocal(userAlertReport, callback)
                    }
                },
                onError = {
                    // Image upload failed- save user alert report without changing URL
                    saveUserAlertReportToFirebaseAndLocal(userAlertReport, callback)
                }
            )
        } else {
            // No image to upload- save the user alert report
            saveUserAlertReportToFirebaseAndLocal(userAlertReport, callback)
        }
    }

    fun deleteUserAlertReport(userAlertReport: UserAlertReport, callback: (Boolean) -> Unit) {
        executor.execute {
            database.userAlertReportDao().delete(userAlertReport)

            mainHandler.post {
                firebaseModel.deleteUserAlertReport(userAlertReport, callback)
            }
        }
    }

    private fun saveUserAlertReportToFirebaseAndLocal(userAlertReport: UserAlertReport, callback: EmptyCallback) {
        firebaseModel.addUserAlertReport(userAlertReport) {
            executor.execute {
                database.userAlertReportDao().insertAll(userAlertReport)
                mainHandler.post {
                    callback()
                }
            }
        }
    }

    fun generateNewAlertReportId(): String {
        return firebaseModel.generateNewAlertReportId()
    }
}