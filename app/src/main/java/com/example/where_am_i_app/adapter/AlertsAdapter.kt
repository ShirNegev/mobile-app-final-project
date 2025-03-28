package com.example.where_am_i_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.where_am_i_app.OnAlertClickListener
import com.example.where_am_i_app.databinding.AlertListRowBinding
import com.example.where_am_i_app.model.Alert

class AlertsAdapter(
    var alerts: List<Alert>?
): RecyclerView.Adapter<AlertViewHolder>() {

    var listener: OnAlertClickListener? = null

    override fun getItemCount(): Int =  alerts?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = AlertListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts?.get(position)
        holder.bind(alert)
    }

    fun update(alerts: List<Alert>) {
        this.alerts = alerts
        notifyDataSetChanged()
    }
}