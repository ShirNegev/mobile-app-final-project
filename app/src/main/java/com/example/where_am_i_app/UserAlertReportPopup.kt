package com.example.where_am_i_app

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.PopupWindow
import androidx.core.graphics.drawable.toDrawable
import com.example.where_am_i_app.databinding.PopupUserAlertReportBinding
import com.example.where_am_i_app.model.UserAlertReport
import com.example.where_am_i_app.utils.getDateFromTimestamp
import com.example.where_am_i_app.utils.getLocationFromGeoHash
import com.squareup.picasso.Picasso
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class UserAlertReportPopup(
    context: Context,
    private val mapView: MapView,
    report: UserAlertReport,
    private val marker: Marker
) {
    private val binding: PopupUserAlertReportBinding =
        PopupUserAlertReportBinding.inflate(LayoutInflater.from(context))
    private val popupWindow: PopupWindow

    init {
        binding.popupUserId.text = report.userId ?: "Unknown"
        binding.popupMessage.text = report.text ?: "No message"
        binding.popupTime.text = getDateFromTimestamp(report.time) ?: "Unknown time"
        binding.popupLocation.text = getLocationFromGeoHash(report.geohashLocation, context) ?: "Unknown location"

        // Load image if available
        report.reportImageUrl.let { url ->
            if (url.isNotBlank()) {
                Picasso.get()
                    .load(url)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(binding.popupImage)
            } else {
                binding.popupImage.setImageResource(R.drawable.image_placeholder)
            }
        } ?: binding.popupImage.setImageResource(R.drawable.image_placeholder)

        binding.popupCloseButton.setOnClickListener {
            dismiss()
        }

        val size = 1200
        popupWindow = PopupWindow(
            binding.root,
            size,
            size,
            true
        ).apply {
            setBackgroundDrawable(0x80000000.toInt().toDrawable())
        }

        show()
    }

    private fun show() {
        popupWindow.showAtLocation(mapView, Gravity.CENTER, 0, 0)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }
}