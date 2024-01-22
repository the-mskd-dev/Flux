package com.kaem.flux.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class HomeState {
    Loading,
    Content
}

class HomeViewModel : ViewModel() {

    var state by mutableStateOf(HomeState.Loading)

    var shows by mutableStateOf<List<String>>(emptyList())

    init {

        viewModelScope.launch {

            delay(1000L)
            state = HomeState.Content

        }

    }

}