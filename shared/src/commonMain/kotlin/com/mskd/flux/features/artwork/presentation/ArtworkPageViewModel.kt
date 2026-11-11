package com.mskd.flux.features.artwork.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Behaviour shared by the Artwork pages (Movie page, Season page, Show page): observing the Artwork,
 * the token / sync status used by the Manual identification, reset of progression, external player result,
 * and the shape of the intent dispatching.
 *
 * A page only overrides how its content is mapped ([uiState]) and how its own intents are handled ([onIntent]).
 */
abstract class ArtworkPageViewModel<UiState, Intent, Event>(
    protected val artworkId: Long,
    observeArtworkUseCase: ObserveArtworkUseCase,
    protected val tokenDataStore: TokenDataStore,
    catalogSyncCoordinator: CatalogSyncCoordinator,
    private val resetProgressUseCase: ResetProgressUseCase,
    private val recordPlaybackResult: SaveProgressUseCase? = null,
) : ViewModel() {

    private val _event = MutableSharedFlow<Event>()
    val event: SharedFlow<Event> = _event.asSharedFlow()

    abstract val uiState: StateFlow<UiState>

    /** The observed Artwork, in its loading / error / content states. */
    protected val observedArtwork: Flow<State<FullArtwork>> = observeArtworkUseCase(artworkId = artworkId)

    /** Whether a token is available, and whether a sync is running. */
    protected val tokenAndSync: Flow<Pair<Boolean, Boolean>> = combine(
        tokenDataStore.flow,
        catalogSyncCoordinator.state,
    ) { token, sync -> token.isNotBlank() to (sync is SyncState.Syncing) }

    fun handleIntent(intent: Intent) = viewModelScope.launch { onIntent(intent) }

    protected abstract suspend fun onIntent(intent: Intent)

    protected suspend fun emit(event: Event) = _event.emit(event)

    protected suspend fun resetProgress(artwork: Artwork, season: Int?) {
        resetProgressUseCase(artwork = artwork, season = season)
    }

    protected suspend fun onExternalPlayerResult(media: Media?, progress: Long) {
        val recorder = recordPlaybackResult ?: return
        media?.let { recorder(media = it, progress = progress) }
    }

}
