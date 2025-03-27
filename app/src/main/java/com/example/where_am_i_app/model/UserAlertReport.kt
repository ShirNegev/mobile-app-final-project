package com.example.where_am_i_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserAlertReport(
    @PrimaryKey val id: String,
    val userId: String,
    val text: String,
    val time: Long,
    val geohashLocation: String,
    val alertTitle: String,
    val reportImageUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "userId" to userId,
            "text" to text,
            "time" to time,
            "geohashLocation" to geohashLocation,
            "alertTitle" to alertTitle,
            "reportImageUrl" to reportImageUrl
        )
    }
}
