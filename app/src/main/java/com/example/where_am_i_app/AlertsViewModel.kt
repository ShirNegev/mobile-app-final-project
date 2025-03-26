package com.example.where_am_i_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.where_am_i_app.model.Alerts
import com.example.where_am_i_app.model.Model

class AlertsViewModel: ViewModel() {

    var alerts: LiveData<Alerts> = Model.shared.alerts

    fun refreshAlerts() {
        Model.shared.getAlerts()
    }
}