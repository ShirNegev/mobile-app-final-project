package com.example.where_am_i_app.model

import com.google.gson.annotations.SerializedName

data class Alerts(

    @SerializedName("features")
    val alerts: List<AlertProperties>,
)
