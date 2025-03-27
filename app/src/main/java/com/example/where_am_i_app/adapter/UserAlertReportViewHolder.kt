package com.example.where_am_i_app.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.where_am_i_app.OnAlertClickListener
import com.example.where_am_i_app.OnUserAlertReportClickListener
import com.example.where_am_i_app.databinding.UserAlertReportListRowBinding
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport
import com.squareup.picasso.Picasso
import com.example.where_am_i_app.R
import com.example.where_am_i_app.utils.getDateFromTimestamp

class UserAlertReportViewHolder(
    private val binding: UserAlertReportListRowBinding,
    listener: OnUserAlertReportClickListener?
) : RecyclerView.ViewHolder(binding.root) {

    private var userAlertReport: UserAlertReport? = null

    init {
        binding.buttonEdit.apply {
            setOnClickListener {
                listener?.onEditClick(userAlertReport);
            }
        }

        binding.buttonDelete.apply {
            setOnClickListener {
                listener?.onDeleteClick(userAlertReport);
            }
        }
    }

    fun bind(userAlertReport: UserAlertReport?) {
        this.userAlertReport = userAlertReport
        binding.textViewAlertTitle.text = userAlertReport?.alertTitle
        binding.textViewMessage.text = userAlertReport?.text
        binding.textViewTime.text = userAlertReport?.time?.let { getDateFromTimestamp(it) }
        binding.textViewLocation.text = userAlertReport?.geohashLocation ?: "no location detected"

        userAlertReport?.reportImageUrl?.let {
            if (it.isNotBlank()) {
                Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.imageViewPhoto)
            }
        }

        if (userAlertReport?.userId != null) {
            Model.shared.getUserById(userId = userAlertReport.userId,
                { user ->
                    binding.textViewUserName.text = user?.name
                },
                {
                    binding.textViewUserName.text = ""
                }
            )
        }
    }

}