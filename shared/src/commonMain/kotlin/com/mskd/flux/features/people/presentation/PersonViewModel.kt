package com.mskd.flux.features.people.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.people.domain.usecase.GetPersonDetailsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonViewModel(
    private val person: Person,
    private val getPersonDetailsUseCase: GetPersonDetailsUseCase,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<PeopleEvent>()
    val event: SharedFlow<PeopleEvent> = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    //endregion

    //region Init

    init {
        loadDetails()
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: PeopleIntent) = viewModelScope.launch {
        when (intent) {
            // Navigation
            PeopleIntent.OnBackTap -> _event.emit(PeopleEvent.BackToPreviousScreen)
            is PeopleIntent.OnArtworkClick -> navigateToArtwork(artwork = intent.artwork, rgb = intent.rgb)
        }
    }

    //endregion

    //region Private Methods

    private fun loadDetails() = viewModelScope.launch {

        _uiState.update { PeopleUiState(state = State.Loading) }

        val details = try {
            getPersonDetailsUseCase(person = person)
        } catch (e: Exception) {
            _uiState.update { PeopleUiState(state = State.Error()) }
            return@launch
        }

        _uiState.update { PeopleUiState(state = State.Content(PeopleContent(details = details))) }

    }

    private suspend fun navigateToArtwork(artwork: Artwork, rgb: Int?) {
        when (artwork.type) {
            ContentType.MOVIE -> _event.emit(PeopleEvent.NavigateToMovie(artworkId = artwork.id, rgb = rgb))
            ContentType.SHOW -> _event.emit(PeopleEvent.NavigateToShow(artworkId = artwork.id, rgb = rgb))
        }
    }

    //endregion

}