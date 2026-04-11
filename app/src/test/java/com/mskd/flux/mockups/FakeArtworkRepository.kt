package com.mskd.flux.mockups

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeArtworkRepository(initialContentType: ContentType = ContentType.MOVIE, unknown: Boolean = false) : ArtworkRepository {

    private val _flow = MutableStateFlow(
        when {
            unknown -> ArtworkRepository.State(
                artwork = MediaMockups.unknownArtwork,
                episodes = MediaMockups.unknownMedias
            )
            initialContentType == ContentType.MOVIE -> ArtworkRepository.State(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            else -> ArtworkRepository.State(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    )

    override val flow: StateFlow<ArtworkRepository.State> = _flow

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        val currentContent = _flow.value
        val currentEpisodes = currentContent.episodes.toMutableList()

        episodes.forEach { savedEpisode ->
            val index = currentEpisodes.indexOfFirst { it.id == savedEpisode.id }
            if (index != -1) {
                currentEpisodes[index] = savedEpisode
            }
        }

        _flow.value = currentContent.copy(episodes = currentEpisodes)
    }

    override suspend fun saveMovie(movie: Movie) {
        _flow.value = _flow.value.copy(movie = movie)
    }

    override suspend fun saveEpisode(episode: Episode) {
        saveEpisodes(listOf(episode))
    }

    override fun searchArtwork(artworkId: Long) {
    }

    fun setContent(state: ArtworkRepository.State) {
        _flow.value = state
    }

    fun setContentType(contentType: ContentType, unknown: Boolean = false) {
        _flow.value = when {
            unknown -> ArtworkRepository.State(
                artwork = MediaMockups.unknownArtwork,
                episodes = MediaMockups.unknownMedias
            )
            contentType == ContentType.MOVIE -> ArtworkRepository.State(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            else -> ArtworkRepository.State(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    }

}