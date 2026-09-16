package com.mskd.flux.features.privateFolder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy.SetArtworkPrivacyUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrivateFolderViewModel(
    private val database: DatabaseRepository,
    private val privateFolderDataStore: PrivateFolderDataStore,
    private val setArtworkPrivacy: SetArtworkPrivacyUseCase
) : ViewModel() {

    //region Variables

    private val _event = MutableSharedFlow<PrivateFolderEvent>()
    val event = _event.asSharedFlow()

    private val _unlocked = MutableStateFlow(false)
    private val _pinError = MutableStateFlow(false)

    //endregion

    //region Flow

    val uiState: StateFlow<PrivateFolderUiState> = combine(
        database.flowPrivateArtworks(),
        privateFolderDataStore.flow,
        _unlocked,
        _pinError
    ) { artworks, privateFolder, unlocked, pinError ->

        PrivateFolderUiState(
            screen = State.Content(Unit),
            locked = privateFolder.enabled && !unlocked,
            artworks = artworks.toImmutableList(),
            pinError = pinError
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrivateFolderUiState()
    )

    //endregion

    //region Intents

    fun handleIntent(intent: PrivateFolderIntent) = viewModelScope.launch {
        when (intent) {
            PrivateFolderIntent.OnBackTap -> _event.emit(PrivateFolderEvent.BackToPreviousScreen)
            is PrivateFolderIntent.SubmitPin -> submitPin(pin = intent.pin)
            is PrivateFolderIntent.OnArtworkTap -> onArtworkTap(artwork = intent.artwork, rgb = intent.rgb)
            is PrivateFolderIntent.RemoveFromPrivateFolder -> removeFromPrivateFolder(artwork = intent.artwork)
        }
    }

    //endregion

    //region Private Methods

    private suspend fun submitPin(pin: String) {
        val pinIsValid = privateFolderDataStore.verifyPin(pin = pin)

        if (pinIsValid) {
            _unlocked.update { true }
            _pinError.update { false }
        } else {
            _pinError.update { true }
            _unlocked.update { false }
        }
    }

    private suspend fun onArtworkTap(artwork: Artwork, rgb: Int?) {
        val event = when (artwork.type) {
            ContentType.SHOW -> PrivateFolderEvent.NavigateToShow(artworkId = artwork.id, rgb = rgb)
            ContentType.MOVIE -> PrivateFolderEvent.NavigateToMovie(artworkId = artwork.id, rgb = rgb)
        }
        _event.emit(event)
    }

    private suspend fun removeFromPrivateFolder(artwork: Artwork) {
        setArtworkPrivacy(artworkId = artwork.id, isPrivate = false)
    }

    //endregion

}
