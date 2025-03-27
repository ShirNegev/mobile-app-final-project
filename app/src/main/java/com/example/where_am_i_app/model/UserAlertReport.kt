package com.example.where_am_i_app.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.where_am_i_app.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class UserAlertReport(
    @PrimaryKey val id: String,
    val userId: String,
    val text: String,
    val time: Long,
    val geohashLocation: String,
    val alertTitle: String,
    val reportImageUrl: String = "",
    val lastUpdated: Long? = null
) {

    companion object {

        var lastUpdated: Long
            get() = MyApplication.Globals.context?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                ?.getLong(LOCAL_LAST_UPDATED, 0) ?: 0

            set(value) {
                MyApplication.Globals.context
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)?.apply {
                        edit().putLong(LOCAL_LAST_UPDATED, value).apply()
                    }
            }

        const val ID_KEY = "id"
        const val USER_ID_KEY = "userId"
        const val TEXT_KEY = "text"
        const val TIME_KEY = "time"
        const val GEOHASH_LOCATION_KEY = "geohashLocation"
        const val ALERT_TITLE_KEY = "alertTitle"
        const val REPORT_IMAGE_URL = "reportImageUrl"
        const val LAST_UPDATED = "lastUpdated"
        const val LOCAL_LAST_UPDATED = "locaStudentLastUpdated"

        fun fromJSON(json: Map<String, Any>): UserAlertReport {
            val id = json[ID_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val text = json[TEXT_KEY] as? String ?: ""
            val time = json[TIME_KEY] as? Long ?: 0
            val geohashLocation = json[GEOHASH_LOCATION_KEY] as? String ?: ""
            val alertTitle = json[ALERT_TITLE_KEY] as? String ?: ""
            val reportImageUrl = json[REPORT_IMAGE_URL] as? String ?: ""
            val timeStamp = json[LAST_UPDATED] as? Timestamp
            val lastUpdatedLongTimestamp = timeStamp?.toDate()?.time

            return UserAlertReport(
                id = id,
                userId = userId,
                text = text,
                time = time,
                geohashLocation = geohashLocation,
                alertTitle = alertTitle,
                reportImageUrl = reportImageUrl,
                lastUpdated = lastUpdatedLongTimestamp
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            USER_ID_KEY to userId,
            TEXT_KEY to text,
            TIME_KEY to time,
            GEOHASH_LOCATION_KEY to geohashLocation,
            ALERT_TITLE_KEY to alertTitle,
            REPORT_IMAGE_URL to reportImageUrl,
            LAST_UPDATED to FieldValue.serverTimestamp()
        )
}
