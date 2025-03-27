package com.example.where_am_i_app.base

import com.example.where_am_i_app.model.UserAlertReport

typealias UserAlertReportsCallback = (List<UserAlertReport>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object Collections {
        const val USERS = "users"
        const val USER_ALERT_REPORTS = "userAlertReports"
    }
}