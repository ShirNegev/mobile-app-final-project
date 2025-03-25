package com.example.where_am_i_app.adapter

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.where_am_i_app.databinding.AlertListRowBinding
import com.example.where_am_i_app.model.Alert
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AlertViewHolder(
    binding: AlertListRowBinding
    ): RecyclerView.ViewHolder(binding.root) {

    private var alert: Alert? = null
    private var dateTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var citiesTextView: TextView? = null

    init {
        dateTextView = binding.tvDate
        timeTextView = binding.tvTime
        citiesTextView = binding.tvCities
    }

    fun bind(alert: Alert?) {
        this.alert = alert
        dateTextView?.text = alert?.startTime?.let { getDateFromTimestamp(it) }
        timeTextView?.text = alert?.startTime?.let { getTimeFromTimestamp(it) }
        citiesTextView?.text = alert?.cities?.let { getShortCitiesString(it) }

    }

    private fun getDateFromTimestamp(timestamp: Long): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault())
        return dateFormatter.format(Instant.ofEpochSecond(timestamp))
    }

    private fun getTimeFromTimestamp(timestamp: Long): String {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
        return timeFormatter.format(Instant.ofEpochSecond(timestamp))
    }

    private fun getShortCitiesString(cities: List<String>): String {
        return if (cities.size > 3) {
            cities.take(3).joinToString(", ") + "..."
        } else {
            cities.joinToString(", ")
        }
    }
}