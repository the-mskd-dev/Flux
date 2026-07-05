package com.mskd.flux.features.artwork.domain.usecase.observeArtwork

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.artwork.FullArtwork
import com.mskd.flux.core.domain.model.core.State
import com.mskd.flux.features.artwork.domain.mapper.buildFullArtworkMovie
import com.mskd.flux.features.artwork.domain.mapper.buildFullArtworkShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

internal class ObserveArtworkUseCaseImpl(
    private val database: DatabaseRepository
) : ObserveArtworkUseCase {

    private val _artworkId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val flow: Flow<State<FullArtwork>> = _artworkId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { artworkId ->
            combine(
                database.flowArtwork(artworkId),
                database.flowMovie(artworkId),
                database.flowSeasons(artworkId),
                database.flowEpisodes(artworkId)
            ) { artwork, movie, seasons, episodes ->

                when (artwork?.type) {
                    ContentType.MOVIE -> {

                        movie?.let {
                            State.Content(content = buildFullArtworkMovie(artwork = artwork, movie = it))
                        } ?: State.Error()

                    }
                    ContentType.SHOW -> {

                        State.Content(
                            content = buildFullArtworkShow(
                                artwork = artwork,
                                seasons = seasons,
                                episodes = episodes
                            )
                        )

                    }
                    null -> State.Error()
                }

            }
        }
        .distinctUntilChanged()

    override fun invoke(artworkId: Long) {
        _artworkId.value = artworkId
    }

}