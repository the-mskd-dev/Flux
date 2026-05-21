package com.mskd.flux.mockups

import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.useCases.artwork.ArtworkUC
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

class FakeArtworkUC(initialContentType: ContentType = ContentType.MOVIE) : ArtworkUC {

    private val _flow = MutableStateFlow(
        when (initialContentType) {
            ContentType.MOVIE -> ArtworkUC.State.MOVIE(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            ContentType.SHOW -> ArtworkUC.State.SHOW(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    )

    override val flow: StateFlow<ArtworkUC.State> = _flow

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        val currentContent = _flow.value
        val currentEpisodes = (currentContent as? ArtworkUC.State.SHOW)?.episodes.orEmpty().toMutableList()

        episodes.forEach { savedEpisode ->
            val index = currentEpisodes.indexOfFirst { it.id == savedEpisode.id }
            if (index != -1) {
                currentEpisodes[index] = savedEpisode
            }
        }

        (currentContent as? ArtworkUC.State.SHOW)?.let {
            _flow.value = currentContent.copy(episodes = currentEpisodes)
        }

    }

    override suspend fun saveMovie(movie: Movie) {
        (_flow.value as? ArtworkUC.State.MOVIE)?.let {
            _flow.value = it.copy(movie = movie)
        }
    }

    override suspend fun saveEpisode(episode: Episode) {
        saveEpisodes(listOf(episode))
    }

    override fun searchArtwork(artworkId: Long) {
        _flow.value = runBlocking { getArtwork(artworkId = artworkId) }
    }

    override suspend fun getArtwork(artworkId: Long): ArtworkUC.State {
        val artwork = MediaMockups.artworks.find { it.id == artworkId }

        return when (artwork?.type) {
            ContentType.MOVIE -> {
                MediaMockups.allMedias.filterIsInstance<Movie>().find { it.artworkId == artworkId }
                    ?.let { ArtworkUC.State.MOVIE(artwork = artwork, movie = it) }
                    ?: ArtworkUC.State.ERROR
            }
            ContentType.SHOW -> {
                val episodes = MediaMockups.allMedias.filterIsInstance<Episode>().filter { it.artworkId == artworkId }
                ArtworkUC.State.SHOW(
                    artwork = artwork,
                    episodes = episodes
                )
            }
            else -> ArtworkUC.State.ERROR
        }
    }

    fun setContent(state: ArtworkUC.State) {
        _flow.value = state
    }

    fun setContentType(contentType: ContentType) {
        _flow.value = when (contentType) {
            ContentType.MOVIE -> ArtworkUC.State.MOVIE(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            ContentType.SHOW -> ArtworkUC.State.SHOW(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    }

}