package com.example.storyapp.view.main

sealed class ResultActivity <out R> {
    data object Loading : ResultActivity<Nothing>()
    data class Success<out T>(val data: T) : ResultActivity<T>()
    data class Error(val error: String) : ResultActivity<Nothing>()
}