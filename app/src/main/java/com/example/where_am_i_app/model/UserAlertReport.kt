package com.example.where_am_i_app.model

data class UserAlertReport(
    val userId: String,
    val text: String,
    val time: Long,
    val location: String,
    val alertTitle: String,
    val alertTime: Long,
    val reportImageUrl: String = ""
)
