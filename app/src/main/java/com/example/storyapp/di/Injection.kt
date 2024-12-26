package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.data.UserPreference
import com.example.storyapp.data.UserRepository
import com.example.storyapp.data.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService() // Token diatur melalui interceptor
        return UserRepository.getInstance(apiService, pref)
    }
}