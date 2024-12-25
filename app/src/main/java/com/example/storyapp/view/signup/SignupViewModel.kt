package com.example.storyapp.view.signup

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.UserRepository

class SignupViewModel(private val repository: UserRepository) : ViewModel() {
    fun signup(name: String, email: String, password: String) = repository.signup(name, email, password)
}