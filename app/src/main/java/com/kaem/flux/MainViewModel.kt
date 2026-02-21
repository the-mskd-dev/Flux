package com.kaem.flux

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.FirebaseRepository
import com.kaem.flux.screens.artwork.ArtworkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository) : ViewModel() {

    private val _event = MutableSharedFlow<ArtworkEvent>()
    val event = _event.asSharedFlow().distinctUntilChanged()

    init {

        viewModelScope.launch {
            firebaseRepository.fetchChangelog()
        }

        viewModelScope.launch {
            firebaseRepository.changelog.collect { changelog ->
                Log.d("MainViewModel", "changelog : ")
                changelog.forEach {
                    Log.d("MainViewModel", "title : ${it.title}")
                }
            }
        }
    }

}