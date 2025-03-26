package com.example.where_am_i_app.model

import com.google.gson.annotations.SerializedName

data class Alert(

    @SerializedName("title")
    val title: String,

    @SerializedName("time")
    val time: Long,

    @SerializedName("type")
    val type: String
)
