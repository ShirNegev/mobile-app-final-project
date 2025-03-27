package com.example.where_am_i_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport


class UserAlertReportsViewModel: ViewModel() {

    var userAlertReports: LiveData<List<UserAlertReport>> = Model.shared.userAlertReports.map { userAlertReports ->
        userAlertReports.sortedByDescending { it.time }
    }

    fun refreshAllUserAlertReports() {
        Model.shared.refreshAllUserAlertReports()
    }

    fun deleteUserAlertReport(userAlertReport: UserAlertReport?, callBack: (Boolean) -> Unit) {
        if (userAlertReport != null) {
            Model.shared.deleteUserAlertReport(userAlertReport, callBack)
        }
    }
}