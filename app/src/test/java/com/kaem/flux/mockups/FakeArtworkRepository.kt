package com.kaem.flux.mockups

import com.kaem.flux.data.repository.artwork.ArtworkRepository
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeArtworkRepository(initialContentType: ContentType) : ArtworkRepository {

    private val _flow = MutableStateFlow(
        when (initialContentType) {
            ContentType.MOVIE -> ArtworkRepository.Content(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            ContentType.SHOW -> ArtworkRepository.Content(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    )

    override val flow: StateFlow<ArtworkRepository.Content> = _flow

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

    override fun searchArtwork(mediaId: Long) {
    }

    fun setContent(content: ArtworkRepository.Content) {
        _flow.value = content
    }

    fun setContentType(contentType: ContentType) {
        _flow.value = when (contentType) {
            ContentType.MOVIE -> ArtworkRepository.Content(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
            ContentType.SHOW -> ArtworkRepository.Content(
                artwork = MediaMockups.showArtwork,
                episodes = MediaMockups.episodes
            )
        }
    }

}