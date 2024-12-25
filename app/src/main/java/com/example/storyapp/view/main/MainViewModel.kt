package com.example.storyapp.view.main

import androidx.lifecycle.*
import com.example.storyapp.api.StoryResponse
import com.example.storyapp.data.UserModel
import com.example.storyapp.data.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun getStories(token: String): LiveData<ResultActivity<StoryResponse>> {
        return repository.getStories(token)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
