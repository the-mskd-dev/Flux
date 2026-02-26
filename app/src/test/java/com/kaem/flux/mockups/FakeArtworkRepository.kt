package com.kaem.flux.mockups

import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeArtworkRepository(
    initialContent: ArtworkRepository.Content
) : ArtworkRepository {

    val repositoryFlow = MutableStateFlow(initialContent)

    override val flow: StateFlow<ArtworkRepository.Content> = repositoryFlow

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        val currentContent = repositoryFlow.value
        val currentEpisodes = currentContent.episodes.toMutableList()

        episodes.forEach { savedEpisode ->
            val index = currentEpisodes.indexOfFirst { it.id == savedEpisode.id }
            if (index != -1) {
                currentEpisodes[index] = savedEpisode
            }
        }

        repositoryFlow.value = currentContent.copy(episodes = currentEpisodes)
    }

    override suspend fun saveMovie(movie: Movie) {
        repositoryFlow.value = repositoryFlow.value.copy(movie = movie)
    }

    override suspend fun saveEpisode(episode: Episode) {
        saveEpisodes(listOf(episode))
    }

    override fun searchArtwork(mediaId: Long) {
    }

}