package com.example.where_am_i_app

import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.example.where_am_i_app.base.MyApplication

import java.io.File

class CloudinaryModel {
    init {
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "api_key" to BuildConfig.API_KEY,
            "api_secret" to BuildConfig.API_SECRET
        )
        MyApplication.Globals.context?.let {
            MediaManager.init(it, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()
        }
    }
    fun uploadImage(
        bitmap: Bitmap,
        userId: String,
        onSuccess: (String?) -> Unit,
        onError: (String?) -> Unit
    ) {
        val context = MyApplication.Globals.context ?: return
        val file: File = bitmap.toFile(context, userId)
        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object  : UploadCallback {
                override fun onStart(requestId: String?) {
                }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                }
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    Log.d("DEBUG", "hey")
                    val url = resultData["secure_url"] as? String ?: ""
                    onSuccess(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.d("DEBUG", "error")
                    onError(error?.description ?: "Unknown error")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                }
            })
            .dispatch()
    }
}