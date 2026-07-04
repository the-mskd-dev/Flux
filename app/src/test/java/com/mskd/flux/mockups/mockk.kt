package com.mskd.flux.mockups

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.data.datastore.SettingsDataStore
import com.mskd.flux.core.data.datastore.SnackbarDataStore
import com.mskd.flux.data.useCases.catalog.CatalogUC
import com.mskd.flux.data.useCases.catalog.CatalogUC.State
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.core.domain.model.files.UserFile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

fun mockkCatalogUC() : CatalogUC = mockk(relaxed = true) {
    every { state } returns MutableStateFlow(State.Idle)
    every { artworks } returns MutableStateFlow(MediaMockups.artworks)
}

fun mockkImagesUC() : ImagesPrefetchManager = mockk(relaxed = true) {
    every { state } returns MutableStateFlow(ImagesPrefetchManager.State.Idle)
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

fun mockkSettingsRepository() : SettingsDataStore = mockk(relaxed = true) {
    every { flow } returns MutableStateFlow(SettingsDataStore.State())
}

fun mockkSnackbarRepository() : SnackbarDataStore = mockk(relaxed = true) {
    every { canShow(any()) } returns MutableStateFlow(true)
    every { getCount(any()) } returns MutableStateFlow(0)
}