package com.example.where_am_i_app.adapter

import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.where_am_i_app.OnAlertClickListener
import com.example.where_am_i_app.databinding.AlertListRowBinding
import com.example.where_am_i_app.model.Alert
import com.example.where_am_i_app.utils.getDateFromTimestamp

class AlertViewHolder(
    binding: AlertListRowBinding,
    listener: OnAlertClickListener?
    ): RecyclerView.ViewHolder(binding.root) {

    private var alert: Alert? = null
    private var titleTextView: TextView? = null
    private var timeTextView: TextView? = null
    private var typeTextView: TextView? = null

    init {
        titleTextView = binding.textViewTitle
        timeTextView = binding.textviewTime
        typeTextView = binding.textViewType

        itemView.setOnClickListener {
            Log.d("TAG", "On click listener on position $adapterPosition")
            listener?.onItemClick(alert)
        }
    }

    fun bind(alert: Alert?) {
        this.alert = alert
        titleTextView?.text = alert?.title
        timeTextView?.text = alert?.time?.let { getDateFromTimestamp(it) }
        typeTextView?.text = alert?.type

    }
}