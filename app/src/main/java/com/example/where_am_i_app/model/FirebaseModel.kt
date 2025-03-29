package com.example.where_am_i_app.model

import android.util.Log
import com.example.where_am_i_app.User
import com.example.where_am_i_app.base.Constants.Collections.USERS
import com.example.where_am_i_app.base.Constants.Collections.USER_ALERT_REPORTS
import com.example.where_am_i_app.base.EmptyCallback
import com.example.where_am_i_app.base.UserAlertReportsCallback
import com.example.where_am_i_app.utils.toFirebaseTimestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.SetOptions
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

    fun getAllUserAlertReports(sinceLastUpdated: Long, callback: UserAlertReportsCallback) {
        database.collection(USER_ALERT_REPORTS)
            .whereGreaterThanOrEqualTo(UserAlertReport.LAST_UPDATED, sinceLastUpdated.toFirebaseTimestamp)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val userAlertReports: MutableList<UserAlertReport> = mutableListOf()
                        for (json in it.result) {
                            userAlertReports.add(UserAlertReport.fromJSON(json.data))
                        }

                        callback(userAlertReports)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getUserAlertReportsById(userAlertReportsId: String, callback: (UserAlertReport?) -> Unit) {
        database.collection(USER_ALERT_REPORTS).document(userAlertReportsId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val userAlertReports = UserAlertReport.fromJSON(document.data!!)
                        callback(userAlertReports)
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("TAG", "Error getting userAlertReport")
                    callback(null)
                }
            }
    }

    fun addUserAlertReport(userAlertReport: UserAlertReport, callback: EmptyCallback) {
        database.collection(USER_ALERT_REPORTS).document(userAlertReport.id).set(userAlertReport.json, SetOptions.merge())
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "failed to add user alert report")

                callback()
            }
    }

    fun deleteUserAlertReport(userAlertReport: UserAlertReport, callback: (Boolean) -> Unit) {
        database.collection(USER_ALERT_REPORTS).document(userAlertReport.id).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    Log.e("TAG", "Error deleting userAlertReport")
                    callback(false)
                }
            }
    }

    fun generateNewAlertReportId(): String {
        return database.collection(USER_ALERT_REPORTS).document().id
    }

    fun listenForUserAlertReportsChanges(callback: (List<UserAlertReport>, List<UserAlertReport>) -> Unit) {
        database.collection(USER_ALERT_REPORTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val updatedUserAlertReports = mutableListOf<UserAlertReport>()
                val deletedUserAlertReports = mutableListOf<UserAlertReport>()

                snapshot?.documentChanges?.forEach { change ->
                    val userAlertReport = UserAlertReport.fromJSON(change.document.data)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            updatedUserAlertReports.add(userAlertReport)
                        }
                        DocumentChange.Type.REMOVED -> {
                            deletedUserAlertReports.add(userAlertReport)
                        }
                    }
                }

                callback(updatedUserAlertReports, deletedUserAlertReports)
            }
    }
}