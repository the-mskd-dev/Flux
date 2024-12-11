package com.kaem.flux.screens.player

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.model.WatchTime
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkInfo
import com.kaem.flux.model.flux.ContentType
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Status
import com.kaem.flux.model.flux.Movie
import com.kaem.flux.utils.timeDescription
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val artwork: Artwork = Artwork(),
    val artworkInfo: ArtworkInfo? = null
) {

    val path = artworkInfo?.file?.path ?: ""

    val currentTime = artworkInfo?.currentTime ?: 0L
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LibraryRepository
) : ViewModel() {

    private val artworkId: Long = checkNotNull(savedStateHandle["id"])
    private val episodeId: Long = checkNotNull(savedStateHandle["episodeId"])

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {

        viewModelScope.launch {

            val libraryContent = repository.libraryContent.value

            val artwork = libraryContent!!.artworks.first { it.id == artworkId }

            val artworkInfo = when (artwork.type) {
                ContentType.MOVIE -> repository.getMovie(artworkId)
                ContentType.SHOW -> repository.getEpisode(episodeId)
            }

            _uiState.value = PlayerUiState(
                artwork = artwork,
                artworkInfo = artworkInfo
            )

        }

    }

    fun saveCurrentTime(time: Long) = viewModelScope.launch {

        uiState.value.let { state ->

            state.artworkInfo?.let { artworkInfo ->
                artworkInfo.currentTime = time
                val watchTime = WatchTime.fromTime(time)
                artworkInfo.status = if (watchTime.timeInMin >= artworkInfo.duration) Status.WATCHED else Status.IS_WATCHING
            }

            when (state.artworkInfo) {

                is Movie -> repository.saveMovie(state.artworkInfo)

                is Episode -> repository.saveEpisode(state.artworkInfo)

                else -> {}

            }

            Log.i("PlayerViewModel", "${state.artwork.title} saved at ${time.timeDescription}")

        }

    }

}