package com.example.where_am_i_app.model

import com.example.where_am_i_app.User
import com.example.where_am_i_app.base.Constants.Collections.USERS
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
}