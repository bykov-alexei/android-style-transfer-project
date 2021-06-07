package com.example.exam

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

class Queries {

    companion object {
        const val API_URL = "http://93.94.183.99:8000"
        lateinit var context: Context
        lateinit var imageView: ImageView
        lateinit var processButton: Button
        lateinit var textView: TextView
        lateinit var shareButton: Button
        lateinit var url: String
    }

    interface Transfer {
        @Multipart
        @POST("/picture")
        fun query(
            @Part image: MultipartBody.Part
        ): Call<String>
    }

}