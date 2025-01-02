package com.example.storyapp.di

sealed class ResultState <out R> {
    data object Loading : ResultState<Nothing>()
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val error: String) : ResultState<Nothing>()
}