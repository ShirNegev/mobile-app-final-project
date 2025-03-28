package com.example.where_am_i_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.where_am_i_app.OnUserAlertReportClickListener
import com.example.where_am_i_app.databinding.UserAlertReportListRowBinding
import com.example.where_am_i_app.model.UserAlertReport

class UserAlertReportAdapter(
    var userAlertReports: List<UserAlertReport>?,
    private val context: Context,
): RecyclerView.Adapter<UserAlertReportViewHolder>() {

    var listener: OnUserAlertReportClickListener? = null

    override fun getItemCount(): Int =  userAlertReports?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAlertReportViewHolder {
        val binding = UserAlertReportListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserAlertReportViewHolder(binding, context, listener)
    }

    override fun onBindViewHolder(holder: UserAlertReportViewHolder, position: Int) {
        val userAlertReport = userAlertReports?.get(position)
        holder.bind(userAlertReport)
    }

    fun update(userAlertReports: List<UserAlertReport>) {
        this.userAlertReports = userAlertReports
        notifyDataSetChanged()
    }
}