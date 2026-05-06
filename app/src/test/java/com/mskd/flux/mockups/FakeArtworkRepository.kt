package com.mskd.flux.mockups

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.ContentType.*
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

class FakeArtworkRepository(initialContentType: ContentType = ContentType.MOVIE) : ArtworkRepository {

    private val _flow = MutableStateFlow(
        when (initialContentType) {
            ContentType.MOVIE -> ArtworkRepository.Content.MOVIE(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            ContentType.SHOW -> ArtworkRepository.Content.SHOW(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    )

    override val flow: StateFlow<ArtworkRepository.Content> = _flow

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        val currentContent = _flow.value
        val currentEpisodes = (currentContent as? ArtworkRepository.Content.SHOW)?.episodes.orEmpty().toMutableList()

        episodes.forEach { savedEpisode ->
            val index = currentEpisodes.indexOfFirst { it.id == savedEpisode.id }
            if (index != -1) {
                currentEpisodes[index] = savedEpisode
            }
        }

        (currentContent as? ArtworkRepository.Content.SHOW)?.let {
            _flow.value = currentContent.copy(episodes = currentEpisodes)
        }

    }

    override suspend fun saveMovie(movie: Movie) {
        (_flow.value as? ArtworkRepository.Content.MOVIE)?.let {
            _flow.value = it.copy(movie = movie)
        }
    }

    override suspend fun saveEpisode(episode: Episode) {
        saveEpisodes(listOf(episode))
    }

    override fun searchArtwork(artworkId: Long) {
        _flow.value = runBlocking { getArtwork(artworkId = artworkId) }
    }

    override suspend fun getArtwork(artworkId: Long): ArtworkRepository.Content {
        val artwork = MediaMockups.artworks.find { it.id == artworkId }

        return when (artwork?.type) {
            ContentType.MOVIE -> {
                MediaMockups.allMedias.filterIsInstance<Movie>().find { it.artworkId == artworkId }
                    ?.let { ArtworkRepository.Content.MOVIE(artwork = artwork, movie = it) }
                    ?: ArtworkRepository.Content.ERROR
            }
            ContentType.SHOW -> {
                val episodes = MediaMockups.allMedias.filterIsInstance<Episode>().filter { it.artworkId == artworkId }
                ArtworkRepository.Content.SHOW(
                    artwork = artwork,
                    episodes = episodes
                )
            }
            else -> ArtworkRepository.Content.ERROR
        }
    }

    fun setContent(state: ArtworkRepository.Content) {
        _flow.value = state
    }

    fun setContentType(contentType: ContentType) {
        _flow.value = when (contentType) {
            ContentType.MOVIE -> ArtworkRepository.Content.MOVIE(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            ContentType.SHOW -> ArtworkRepository.Content.SHOW(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    }

}