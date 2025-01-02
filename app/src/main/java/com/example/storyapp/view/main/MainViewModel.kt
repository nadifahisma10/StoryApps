package com.example.storyapp.view.main

import androidx.lifecycle.*
import com.example.storyapp.api.StoryResponse
import com.example.storyapp.data.UserModel
import com.example.storyapp.data.UserRepository
import com.example.storyapp.di.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _stories = MutableLiveData<ResultState<StoryResponse>?>()
    val stories: LiveData<ResultState<StoryResponse>?> = _stories

    suspend fun login(email: String, password: String) = repository.login(email, password)
    suspend fun signup(name: String, email: String, password: String) = repository.signup(name, email, password)

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun getStories(token: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = repository.getStories(token)
        _stories.postValue(result)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
