package com.mskd.flux.mockups

import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.useCases.artwork.ArtworkUC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

class FakeArtworkUC(initialContentType: ContentType = ContentType.MOVIE) : ArtworkUC {

    private val _flow = MutableStateFlow<State<FullArtwork>>(
        when (initialContentType) {
            ContentType.MOVIE -> State.Content(
                FullArtwork.FullMovie(
                    resume = MediaMockups.movieArtwork,
                    movie = MediaMockups.movie
                )
            )
            ContentType.SHOW -> State.Content(
                FullArtwork.FullShow(
                    resume = MediaMockups.showArtwork,
                    seasons = MediaMockups.seasons,
                    episodes = MediaMockups.episodes
                )
            )
        }
    )

    override val flow: StateFlow<State<FullArtwork>> = _flow

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        val currentState = _flow.value
        if (currentState is State.Content && currentState.content is FullArtwork.FullShow) {
            val currentShow = currentState.content
            val currentEpisodes = currentShow.episodes.toMutableList()
            episodes.forEach { savedEpisode ->
                val index = currentEpisodes.indexOfFirst { it.id == savedEpisode.id }
                if (index != -1) {
                    currentEpisodes[index] = savedEpisode
                }
            }
            _flow.value = State.Content(
                currentShow.copy(episodes = currentEpisodes)
            )
        }
    }

    override suspend fun saveMovie(movie: Movie) {
        val currentState = _flow.value
        if (currentState is State.Content && currentState.content is FullArtwork.FullMovie) {
            val currentMovie = currentState.content
            _flow.value = State.Content(
                currentMovie.copy(movie = movie)
            )
        }
    }

    override suspend fun saveEpisode(episode: Episode) {
        saveEpisodes(listOf(episode))
    }

    override fun searchArtwork(artworkId: Long) {
        _flow.value = runBlocking {
            getArtwork(artworkId = artworkId)?.let { State.Content(it) } ?: State.Error
        }
    }

    override suspend fun getArtwork(artworkId: Long): FullArtwork? {
        val artwork = MediaMockups.artworks.find { it.id == artworkId }

        return when (artwork?.type) {
            ContentType.MOVIE -> {
                MediaMockups.allMedias.filterIsInstance<Movie>().find { it.artworkId == artworkId }
                    ?.let { FullArtwork.FullMovie(resume = artwork, movie = it) }
            }
            ContentType.SHOW -> {
                val episodes = MediaMockups.allMedias.filterIsInstance<Episode>().filter { it.artworkId == artworkId }
                FullArtwork.FullShow(
                    resume = artwork,
                    seasons = MediaMockups.seasons.filter { it.artworkId == artworkId },
                    episodes = episodes
                )
            }
            else -> null
        }
    }

    fun setContent(state: State<FullArtwork>) {
        _flow.value = state
    }

    fun setContentType(contentType: ContentType) {
        _flow.value = when (contentType) {
            ContentType.MOVIE -> State.Content(
                FullArtwork.FullMovie(
                    resume = MediaMockups.movieArtwork,
                    movie = MediaMockups.movie
                )
            )
            ContentType.SHOW -> State.Content(
                FullArtwork.FullShow(
                    resume = MediaMockups.showArtwork,
                    seasons = MediaMockups.seasons,
                    episodes = MediaMockups.episodes
                )
            )
        }
    }

}