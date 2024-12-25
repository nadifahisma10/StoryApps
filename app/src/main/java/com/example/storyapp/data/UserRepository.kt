package com.example.storyapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.storyapp.api.ApiService
import com.example.storyapp.api.LoginResponse
import com.example.storyapp.api.SignupResponse
import com.example.storyapp.api.StoryResponse
import com.example.storyapp.view.main.ResultActivity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    fun login(email: String, password: String): LiveData<ResultActivity<LoginResponse>> = liveData {
        emit(ResultActivity.Loading)
        try {
            val response = apiService.login(email, password)
            emit(ResultActivity.Success(response))
        } catch (e: HttpException) {
            Log.e("postLogin", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, LoginResponse::class.java)
                emit(ResultActivity.Success(parsedError))
            } catch (parseException: Exception) {
                Log.e("postLogin", "Error parsing response: ${parseException.message}")
                emit(ResultActivity.Error("Error parsing HTTP exception response"))
            }
        } catch (e: Exception) {
            Log.e("postLogin", "General Exception: ${e.message}")
            emit(ResultActivity.Error(e.message.toString()))
        }
    }

    fun signup(name: String, email: String, password: String): LiveData<ResultActivity<SignupResponse>> = liveData {
        emit(ResultActivity.Loading)
        try {
            val response = apiService.signup(name, email, password)
            emit(ResultActivity.Success(response))
        } catch (e: HttpException) {
            Log.e("postSignup", "HTTP Exception: ${e.message}")
            try {
                val errorResponse = e.response()?.errorBody()?.string()
                val gson = Gson()
                val parsedError = gson.fromJson(errorResponse, SignupResponse::class.java)
                emit(ResultActivity.Success(parsedError))
            } catch (parseException: Exception) {
                Log.e("postSignup", "Error parsing response: ${parseException.message}")
                emit(ResultActivity.Error("Error parsing HTTP exception response"))
            }
        } catch (e: Exception) {
            Log.e("postSignup", "General Exception: ${e.message}")
            emit(ResultActivity.Error(e.message.toString()))
        }
    }

    fun getStories(token: String): LiveData<ResultActivity<StoryResponse>> = liveData {
        emit(ResultActivity.Loading)
        try {
            val response = apiService.getStories("Bearer $token")
            emit(ResultActivity.Success(response))
        } catch (e: HttpException) {
            val errorResponse = e.response()?.errorBody()?.string()
            Log.e("getAllStories", "Raw Error Response: $errorResponse")
            if (errorResponse != null && errorResponse.startsWith("<html>")) {
                // Deteksi HTML dan tampilkan pesan error yang lebih spesifik
                emit(ResultActivity.Error("Server error: Unexpected HTML response. Please contact support."))
            } else {
                try {
                    val parsedError = Gson().fromJson(errorResponse, StoryResponse::class.java)
                    emit(ResultActivity.Success(parsedError))
                } catch (parseException: Exception) {
                    Log.e("getAllStories", "Error parsing error response: ${parseException.message}")
                    emit(ResultActivity.Error("Failed to parse server response."))
                }
            }
        } catch (e: Exception) {
            Log.e("getAllStories", "General Exception: ${e.message}")
            emit(ResultActivity.Error(e.message.toString()))
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
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
