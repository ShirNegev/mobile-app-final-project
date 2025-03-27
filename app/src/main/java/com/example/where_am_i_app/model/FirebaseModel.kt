package com.example.where_am_i_app.model

import android.util.Log
import com.example.where_am_i_app.User
import com.example.where_am_i_app.base.Constants.Collections.USERS
import com.example.where_am_i_app.base.Constants.Collections.USER_ALERT_REPORTS
import com.example.where_am_i_app.base.EmptyCallback
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase

class FirebaseModel {

    private val database = Firebase.firestore

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun addUser(user: User, callback: EmptyCallback) {
        database.collection(USERS).document(user.id).set(user.toMap())
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener { e ->
            }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit, erorrCallback: (String?) -> Unit) {
        database.collection(USERS).document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = User.fromMap(document.data ?: mapOf(), userId)
                    callback(user)
                } else {
                    Log.e("TAG", "User document doesn't exist")
                }
            }
            .addOnFailureListener { e ->
                erorrCallback(e.message)
            }
    }

    fun addUserAlertReport(userAlertReport: UserAlertReport, callback: EmptyCallback) {
        database.collection(USER_ALERT_REPORTS).document(userAlertReport.id).set(userAlertReport.toMap())
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener { e -> Log.e("TAG", "failed to add user alert report") }
    }

    fun getAllUserAlertReports(callback: (List<UserAlertReport>) -> Unit, onError: (String) -> Unit) {
        database.collection(USER_ALERT_REPORTS)
            .get()
            .addOnSuccessListener { result ->
                val userAlertReports = mutableListOf<UserAlertReport>()
                for (document in result) {
                    val userAlertReport = document.toObject(UserAlertReport::class.java)
                    userAlertReports.add(userAlertReport)
                }
                callback(userAlertReports) // Pass the list of reports to the callback
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting documents: ", exception)
                onError("Failed to fetch user alert reports")
            }
    }

    fun generateNewAlertReportId(): String {
        return database.collection(USER_ALERT_REPORTS).document().id
    }
}