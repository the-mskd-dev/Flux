package com.kaem.flux.screens.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.utils.extensions.msToMin
import com.kaem.flux.utils.extensions.timeDescription
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted private val media: Media,
    private val repository: ArtworkRepository,
    private val userRepository: UserRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    //region Factory

    @AssistedFactory
    interface Factory {
        fun create(media: Media): PlayerViewModel
    }

    //endregion

    //region Flow

    val uiState: StateFlow<PlayerUiState> = settingsRepository.flow.map { settings ->
        PlayerUiState(
            state = PlayerScreenState.Content(media = media),
            playerForward = settings.playerForwardValue.seconds.inWholeMilliseconds,
            playerBackward = settings.playerBackwardValue.seconds.inWholeMilliseconds,
            subtitlesLanguage = settings.subtitlesLanguage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerUiState()
    )

    //endregion

    //region Public methods

    fun handleIntent(intent: PlayerIntent) = viewModelScope.launch {
        when (intent) {
            is PlayerIntent.SaveTime -> saveTime(time = intent.time)
        }
    }

    //endregion

    //region Private methods

    private suspend fun saveTime(time: Long) {

        val newStatus = if (time.msToMin >= media.duration * .9) Status.WATCHED else Status.IS_WATCHING
        val newTime = if (newStatus == Status.WATCHED) 0L else time

        val updatedMedia = when (media) {
            is Movie -> media.copy(currentTime = newTime, status = newStatus)
            is Episode -> media.copy(currentTime = newTime, status = newStatus)
        }

        when (updatedMedia) {
            is Movie -> {
                repository.saveMovie(updatedMedia)
                if (newStatus == Status.WATCHED) userRepository.removeWatchedMedia(media.artworkId)
                else userRepository.addWatchedMedia(media.artworkId)
            }
            is Episode -> {
                val episodes = repository.flow.first().episodes
                val lastEpisode = episodes.maxWith(compareBy<Episode> { it.season }.thenBy { it.number })
                if (lastEpisode.id == updatedMedia.id && newStatus == Status.WATCHED)
                    userRepository.removeWatchedMedia(media.artworkId)
                else
                    userRepository.addWatchedMedia(media.artworkId)
                repository.saveEpisode(updatedMedia)
            }
        }

        Log.i("PlayerViewModel", "${updatedMedia.title} saved at ${time.timeDescription()}")

    }

    //endregion

}