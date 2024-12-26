package com.example.storyapp.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.api.LoginResponse
import com.example.storyapp.data.UserModel
import com.example.storyapp.data.UserRepository
import com.example.storyapp.view.main.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<ResultState<LoginResponse>>()
    val loginResult: LiveData<ResultState<LoginResponse>> = _loginResult

    fun saveSession(user: UserModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSession(user)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.login(email, password)
        println("ARFDEV Login vm: $result")
        _loginResult.postValue(result)
    }
}