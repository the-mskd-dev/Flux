package com.mskd.flux.mockups

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.snackbars.SnackbarRepository
import com.mskd.flux.model.UserFile
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.catalog.CatalogUC.State
import com.mskd.flux.useCases.images.ImagesUC
import com.mskd.flux.useCases.progress.ProgressUC
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

fun mockkProgressUC() : ProgressUC = mockk(relaxed = true)

fun mockkCatalogUC() : CatalogUC = mockk(relaxed = true) {
    every { state } returns MutableStateFlow(State.Idle)
    every { artworks } returns MutableStateFlow(MediaMockups.artworks)
}

fun mockkImagesUC() : ImagesUC = mockk(relaxed = true) {
    every { state } returns MutableStateFlow(ImagesUC.State.Idle)
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

fun mockkSettingsRepository() : SettingsRepository = mockk(relaxed = true) {
    every { flow } returns MutableStateFlow(SettingsRepository.State())
}

fun mockkSnackbarRepository() : SnackbarRepository = mockk(relaxed = true) {
    every { canShow(any()) } returns MutableStateFlow(true)
    every { getCount(any()) } returns MutableStateFlow(0)
}

fun mockkFilesRepository() : FilesRepository = mockk(relaxed = true) {
    coEvery { getFiles() } returns FilesMockups.localFiles
    coEvery { filterExistingFiles(any()) } answers {
        val files = firstArg<List<UserFile>>()
        files.filter { f -> FilesMockups.localFiles.any { it.name == f.name } }
    }
    coEvery { getSubtitlesFor(any()) } returns null
}