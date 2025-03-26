package com.example.where_am_i_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.where_am_i_app.model.Alert
import com.example.where_am_i_app.model.Model

class AlertsViewModel: ViewModel() {

    var alerts: LiveData<List<Alert>> = Model.shared.alerts

    fun refreshAlerts() {
        Model.shared.getAlerts()
    }
}