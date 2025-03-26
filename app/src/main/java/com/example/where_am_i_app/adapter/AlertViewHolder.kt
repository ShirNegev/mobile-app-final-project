package com.example.where_am_i_app.adapter

import android.util.Log
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
    private var titleTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var typeTextView: TextView? = null

    init {
        titleTextView = binding.textViewTitle
        timeTextView = binding.textviewTime
        typeTextView = binding.textViewType
    }

    fun bind(alert: Alert?) {
        Log.e("TAG", "Alert! ${alert}")
        this.alert = alert
        titleTextView?.text = alert?.title
        timeTextView?.text = alert?.time?.let { getDateFromTimestamp(it) }
        typeTextView?.text = alert?.type

    }

    private fun getDateFromTimestamp(timestamp: Long): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
        return dateFormatter.format(Instant.ofEpochSecond(timestamp / 1000))
    }
}