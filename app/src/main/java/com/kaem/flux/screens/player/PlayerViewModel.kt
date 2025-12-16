package com.kaem.flux.screens.player

import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.model.media.Media
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted val media: Media,
    val repository: MediaRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(media: Media): PlayerViewModel
    }

}