package com.example.where_am_i_app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.where_am_i_app.databinding.AlertListRowBinding
import com.example.where_am_i_app.model.Alerts

class AlertsAdapter(
    var alerts: Alerts?
): RecyclerView.Adapter<AlertViewHolder>() {

    override fun getItemCount(): Int {
        Log.e("RecyclerView", "Item count: ${alerts?.alerts?.size ?: 0}")
        return alerts?.alerts?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = AlertListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.e("RecyclerView", "alert created ${binding}")
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        Log.e("TAG", "Alert bindddd! ${alerts?.alerts?.get(position)}")
        val alert = alerts?.alerts?.get(position)
        holder.bind(alert?.properties)
    }

    fun update(alerts: Alerts) {
        this.alerts = alerts
        notifyDataSetChanged()
        Log.e("TAG", "Alert updated! ${this.alerts?.alerts}")
    }
}