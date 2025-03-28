package com.example.where_am_i_app.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getDateFromTimestamp(timestamp: Long): String {
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
    return dateFormatter.format(Instant.ofEpochSecond(timestamp / 1000))
}