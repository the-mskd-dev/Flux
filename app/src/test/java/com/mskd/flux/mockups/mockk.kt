package com.mskd.flux.mockups

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.catalog.CatalogUC.State
import com.mskd.flux.useCases.progress.ProgressUC
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

fun mockkProgressUC() : ProgressUC = mockk(relaxed = true)

fun mockkCatalogUC() : CatalogUC = mockk(relaxed = true) {
    every { state } returns MutableStateFlow(State.Idle)
    every { artworks } returns MutableStateFlow(MediaMockups.artworks.filter { !it.isUnknown })
}

fun mockkDatabaseRepository() : DatabaseRepository = mockk(relaxed = true) {

    // Flow
    every { flowArtworks() } returns MutableStateFlow(MediaMockups.artworks)
    every { flowArtwork(any()) } answers {
        val artworkId = firstArg<Long>()
        MutableStateFlow(MediaMockups.artworks.find { it.id == artworkId })
    }
    every { flowMovie(any()) } answers {
        val artworkId = firstArg<Long>()
        MutableStateFlow(MediaMockups.movies.find { it.artworkId == artworkId })
    }
    every { flowEpisodes(any()) } answers {
        val artworkId = firstArg<Long>()
        MutableStateFlow(MediaMockups.episodes.filter { it.artworkId == artworkId })
    }


    // Artworks
    coEvery { getArtwork(any()) } answers  {
        val artworkId = firstArg<Long>()
        MediaMockups.artworks.find { it.id == artworkId }
    }
    coEvery { getArtworks() } returns MediaMockups.artworks

    // Movies
    coEvery { getMovie(any()) } answers  {
        val artworkId = firstArg<Long>()
        MediaMockups.movies.find { it.artworkId == artworkId }
    }
    coEvery { getMovies() } returns MediaMockups.movies

    // Episodes
    coEvery { getEpisode(any()) } answers  {
        val episodeId = firstArg<Long>()
        MediaMockups.episodes.find { it.id == episodeId }
    }
    coEvery { getEpisodes(any()) } answers  {
        val artworkId = firstArg<Long>()
        MediaMockups.episodes.filter { it.artworkId == artworkId }
    }
    coEvery { getEpisodes() } returns MediaMockups.episodes
    coEvery { getEpisodeCount(any()) } answers  {
        val artworkId = firstArg<Long>()
        MediaMockups.episodes.count { it.artworkId == artworkId }
    }
}