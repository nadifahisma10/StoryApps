package com.example.storyapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.storyapp.api.ApiService
import com.example.storyapp.api.LoginResponse
import com.example.storyapp.api.SignupResponse
import com.example.storyapp.api.StoryResponse
import com.example.storyapp.api.UploadResponse
import com.example.storyapp.view.main.ResultState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    suspend fun login(email: String, password: String): ResultState<LoginResponse> {
        try {
            val response = apiService.login(email, password)
            return if (response.error == true) {
                ResultState.Error(response.message.orEmpty())
            } else {
                ResultState.Success(response)
            }
        } catch (e: HttpException) {
            Log.e("postLogin", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string().orEmpty()
                return ResultState.Error(errorResponse)
            } catch (parseException: Exception) {
                Log.e("postLogin", "Error parsing response: ${parseException.message}")
                return ResultState.Error(parseException.message.orEmpty())
            }
        } catch (e: Exception) {
            Log.e("postLogin", "General Exception: ${e.message}")
            return ResultState.Error(e.message.toString())
        }
    }

    suspend fun signup(name: String, email: String, password: String): ResultState<SignupResponse> {
        try {
            val response = apiService.register(name, email, password)
            return if (response.error == true) {
                ResultState.Error(response.message.orEmpty())
            } else {
                ResultState.Success(response)
            }
        } catch (e: HttpException) {
            Log.e("postSignup", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string().orEmpty()
                return ResultState.Error(errorResponse)
            } catch (parseException: Exception) {
                Log.e("postSignup", "Error parsing response: ${parseException.message}")
                return ResultState.Error(parseException.message.orEmpty())
            }
        } catch (e: Exception) {
            Log.e("postSignup", "General Exception: ${e.message}")
            return ResultState.Error(e.message.toString())
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun getStories(token: String): ResultState<StoryResponse> {
        try {
            val response = apiService.getStories(token)
            return ResultState.Success(response)
        } catch (e: HttpException) {
            val errorResponse = e.response()?.errorBody()?.string()
            Log.e("getAllStories", "Raw Error Response: $errorResponse")
            if (errorResponse != null && errorResponse.startsWith("<html>")) {
                // Deteksi HTML dan tampilkan pesan error yang lebih spesifik
                return ResultState.Error("Server error: Unexpected HTML response. Please contact support.")
            } else {
                try {
                    val parsedError = Gson().fromJson(errorResponse, StoryResponse::class.java)
                    return ResultState.Success(parsedError)
                } catch (parseException: Exception) {
                    Log.e(
                        "getAllStories",
                        "Error parsing error response: ${parseException.message}"
                    )
                    return ResultState.Error("Failed to parse server response.")
                }
            }
        } catch (e: Exception) {
            Log.e("getAllStories", "General Exception: ${e.message}")
            return ResultState.Error(e.message.toString())
        }
    }

    fun uploadStory(
        imageFile: File,
        description: String
    ): LiveData<ResultState<UploadResponse>> = liveData {
        emit(ResultState.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val response = apiService.uploadStory(multipartBody, requestBody)
            emit(ResultState.Success(response))
        } catch (e: HttpException) {
            Log.e("uploadStory", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, UploadResponse::class.java)
                emit(ResultState.Success(parsedError))
            } catch (e: Exception) {
                Log.e("uploadStory", "Error parsing error response: ${e.message}")
                emit(ResultState.Error("Error: ${e.message}"))
            }
        } catch (e: Exception) {
            Log.e("uploadStory", "General Exception: ${e.message}")
            emit(ResultState.Error(e.message.toString()))
        }
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference)
            }.also { instance = it }
    }
}
