package com.example.storyapp.api

import com.google.gson.annotations.SerializedName

data class UploadResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)