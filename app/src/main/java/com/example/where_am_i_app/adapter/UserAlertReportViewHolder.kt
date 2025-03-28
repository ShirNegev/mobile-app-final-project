package com.example.where_am_i_app.adapter

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import ch.hsr.geohash.GeoHash
import com.example.where_am_i_app.OnUserAlertReportClickListener
import com.example.where_am_i_app.databinding.UserAlertReportListRowBinding
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport
import com.squareup.picasso.Picasso
import com.example.where_am_i_app.R
import com.example.where_am_i_app.model.AuthManager
import com.example.where_am_i_app.utils.getDateFromTimestamp
import com.example.where_am_i_app.utils.getLocationFromGeoHash

class UserAlertReportViewHolder(
    private val binding: UserAlertReportListRowBinding,
    private val context: Context,
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
        binding.textViewLocation.text = userAlertReport?.geohashLocation?.let {
            getLocationFromGeoHash(it, context)
        }

        // reset view visibility
        binding.buttonEdit.visibility = View.VISIBLE
        binding.buttonDelete.visibility = View.VISIBLE

        if (userAlertReport?.userId != AuthManager.shared.userId) {
            binding.buttonEdit.visibility = View.GONE
            binding.buttonDelete.visibility = View.GONE
        }

        userAlertReport?.reportImageUrl?.let {
            if (it.isNotBlank()) {
                Log.e("image", "${it}")
                Picasso.get()
                    .load(it)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.image_placeholder)
                    .into(binding.imageViewPhoto)
            } else {
                binding.imageViewPhoto.setImageResource(R.drawable.image_placeholder)
            }
        }

        if (userAlertReport?.userId != null) {
            Model.shared.getUserById(
                userId = userAlertReport.userId,
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