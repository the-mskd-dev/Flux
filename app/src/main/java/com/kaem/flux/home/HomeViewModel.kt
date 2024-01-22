package com.kaem.flux.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.model.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeState {
    Loading,
    Content
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
): ViewModel() {

    var state by mutableStateOf(HomeState.Loading)

    var shows by mutableStateOf<List<String>>(emptyList())

    init {

        viewModelScope.launch {

            repository.getLocalFiles().collect {

                when (it) {

                    is DataState.Loading -> {

                        state = HomeState.Loading

                    }

                    is DataState.Success -> {

                        shows = it.data
                        state = HomeState.Content

                    }


                    is DataState.Error -> {

                        state = HomeState.Content

                    }
                }

            }

        }

    }

}