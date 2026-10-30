package com.mskd.flux.features.identification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.identification.domain.usecase.IdentifyManuallyUseCase
import com.mskd.flux.report.Trace
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IdentificationViewModel(
    private val artworkId: Long,
    private val databaseRepository: DatabaseRepository,
    private val apiRepository: ApiRepository,
    private val identifyManually: IdentifyManuallyUseCase,
) : ViewModel() {

    private companion object { const val TAG = "IdentificationViewModel" }

    //region Flow

    private val _event = MutableSharedFlow<IdentificationEvent>()
    val event: SharedFlow<IdentificationEvent> = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(IdentificationUiState())
    val uiState: StateFlow<IdentificationUiState> = _uiState.asStateFlow()

    private var artwork: Artwork? = null
    private var searchJob: Job? = null

    //endregion

    //region Init

    init {
        viewModelScope.launch {
            val source = databaseRepository.getArtwork(artworkId = artworkId)
            artwork = source
            // The title of the file name, not the TMDB title: it is what the Synchro searched
            val file = databaseRepository.getMovie(artworkId = artworkId)?.file
                ?: databaseRepository.getEpisodes(artworkId = artworkId).firstOrNull()?.file
            _uiState.update { it.copy(query = file?.nameProperties?.title.orEmpty()) }
            if (source == null) _uiState.update { it.copy(results = State.Error()) } else search()
        }
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: IdentificationIntent) {
        when (intent) {
            is IdentificationIntent.UpdateQuery -> _uiState.update { it.copy(query = intent.query) }
            IdentificationIntent.Search -> search()
            is IdentificationIntent.Select -> select(candidate = intent.candidate)
        }
    }

    //endregion

    //region Private Methods

    private fun search() {

        val artwork = artwork ?: return
        val query = _uiState.value.query.trim()

        searchJob?.cancel()
        _uiState.update { it.copy(results = State.Loading) }

        searchJob = viewModelScope.launch {
            val results = try {
                val candidates = if (query.isEmpty()) emptyList() else apiRepository.searchArtworks(title = query, type = artwork.type)
                State.Content(
                    candidates.map { candidate ->
                        IdentificationCandidate(artwork = candidate.artwork, year = candidate.year, isCurrent = candidate.artwork.id == artwork.id)
                    }.toImmutableList()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Trace.error(TAG, "Fail to search '$query' for artworkId:$artworkId", e)
                State.Error()
            }
            _uiState.update { it.copy(results = results) }
        }

    }

    private fun select(candidate: IdentificationCandidate) {

        if (candidate.isCurrent || _uiState.value.isIdentifying) return

        _uiState.update { it.copy(isIdentifying = true, identificationFailed = false) }

        viewModelScope.launch {
            identifyManually(artworkId = artworkId, target = candidate.artwork)
                .onSuccess { _event.emit(IdentificationEvent.Identified(artworkId = it)) }
                .onFailure { _uiState.update { it.copy(isIdentifying = false, identificationFailed = true) } }
        }

    }

    //endregion

}
