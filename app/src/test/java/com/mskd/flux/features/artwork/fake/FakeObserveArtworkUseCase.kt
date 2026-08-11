package com.mskd.flux.features.artwork.fake

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class FakeObserveArtworkUseCase() : ObserveArtworkUseCase {

    private val _artworkId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val flow: Flow<State<FullArtwork>> = _artworkId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { artworkId ->

            val artwork = MediaMockups.artworks.find { it.id == artworkId }

            val fullArtwork = when (artwork?.type) {
                ContentType.MOVIE -> {
                    MediaMockups.allMedias.filterIsInstance<Movie>()
                        .find { it.artworkId == artworkId }
                        ?.let { FullArtwork.FullMovie(artwork = artwork, movie = it, genres = DetailsMockup.movieGenres) }
                }

                ContentType.SHOW -> {
                    val episodes = MediaMockups.allMedias.filterIsInstance<Episode>()
                        .filter { it.artworkId == artworkId }
                    FullArtwork.FullShow(
                        artwork = artwork,
                        seasons = MediaMockups.seasons.filter { it.artworkId == artworkId }.toImmutableList(),
                        episodes = episodes.toImmutableList(),
                        genres = DetailsMockup.showGenres
                    )
                }

                else -> null

            }

            if (fullArtwork != null) {
                MutableStateFlow(State.Content(content = fullArtwork))
            } else {
                MutableStateFlow(State.Error())
            }

        }
        .distinctUntilChanged()

    override fun invoke(artworkId: Long) {
        _artworkId.value = artworkId
    }

}