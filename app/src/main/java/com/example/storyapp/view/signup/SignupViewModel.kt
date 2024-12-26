package com.example.storyapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.api.SignupResponse
import com.example.storyapp.data.UserRepository
import com.example.storyapp.view.main.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<ResultState<SignupResponse>>()
    val registerResult: LiveData<ResultState<SignupResponse>> = _registerResult

    fun signup(name: String, email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.signup(name, email, password)
        _registerResult.postValue(result)
    }
}