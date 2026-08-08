package com.mskd.flux.features.artwork.domain.usecase.observeArtwork

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.mapper.buildFullArtworkMovie
import com.mskd.flux.features.artwork.domain.mapper.buildFullArtworkShow
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ObserveArtworkUseCaseImpl(
    private val database: DatabaseRepository,
    private val detailsRepository: DetailsRepository,
    private val sourcesUseCase: FlowSourcesUseCase
) : ObserveArtworkUseCase {

    private val _artworkId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val flow: Flow<State<FullArtwork>> = _artworkId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { artworkId ->
            combine(
                database.flowArtwork(artworkId),
                database.flowMedias(artworkId),
                database.flowSeasons(artworkId),
                detailsRepository.flowGenres(),
                sourcesUseCase()
            ) { artwork, medias, seasons, genres, sources ->

                when (artwork?.type) {
                    ContentType.MOVIE -> {

                        medias.filterIsInstance<Movie>().firstOrNull()?.let {
                            State.Content(
                                content = buildFullArtworkMovie(
                                    artwork = artwork,
                                    movie = it,
                                    genres = genres,
                                    sources = sources
                                )
                            )
                        } ?: State.Error()

                    }
                    ContentType.SHOW -> {

                        State.Content(
                            content = buildFullArtworkShow(
                                artwork = artwork,
                                seasons = seasons,
                                episodes = medias.filterIsInstance<Episode>(),
                                genres = genres,
                                sources = sources
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