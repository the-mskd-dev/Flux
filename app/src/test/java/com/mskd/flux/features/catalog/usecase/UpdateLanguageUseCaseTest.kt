package com.mskd.flux.features.catalog.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.network.tmdb.domain.model.Translation
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.catalog.fake.FakeCatalogSyncCoordinator
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateLanguageUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var api: ApiRepository
    lateinit var database: DatabaseRepository
    lateinit var settings: SettingsDataStore
    lateinit var coordinator: CatalogSyncCoordinator
    lateinit var syncGenresUseCase: SyncGenresUseCase
    lateinit var useCase: UpdateLanguageUseCase

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    val movie = Movie(
        artworkId = 1L,
        title = "Old Movie Title",
        description = "Old Movie Desc",
        releaseDateString = "",
        voteAverage = 0f,
        voteCount = 0,
        duration = 120,
        file = mockk(),
        isAvailable = true
    )
    val showArtwork = Artwork(
        id = 2L,
        title = "Old Show Title",
        description = "Old Show Desc",
        type = ContentType.SHOW,
        imagePath = "",
        bannerPath = ""
    )
    val season = Season(
        id = 10L,
        artworkId = 2L,
        season = 1,
        title = "Old Season Title",
        description = "Old Season Desc",
        imagePath = ""
    )
    val episode = Episode(
        id = 3L,
        artworkId = 2L,
        season = 1,
        number = 1,
        title = "Old Episode Title",
        description = "Old Episode Desc",
        imagePath = "",
        releaseDateString = "",
        duration = 20,
        voteAverage = 0f,
        voteCount = 0,
        file = mockk(),
        isAvailable = true
    )

    beforeTest {
        api = mockk()
        database = mockk(relaxed = true)
        settings = mockk(relaxed = true)
        syncGenresUseCase = mockk(relaxed = true)
        coordinator = FakeCatalogSyncCoordinator(scope = testScope)

        coEvery { settings.getDataLanguage() } returns Locale.FRENCH
        coEvery { database.getMedias() } returns listOf(movie) + listOf(episode)
        coEvery { database.getArtworks() } returns listOf(showArtwork)
        coEvery { database.getSeasons() } returns listOf(season)

        useCase = UpdateLanguageUseCase(
            api = api,
            database = database,
            settings = settings,
            coordinator = coordinator,
            syncGenresUseCase = syncGenresUseCase,
            dispatcher = testDispatcher
        )
    }

    test("retrieves translations for movie/show/season/episode and saves them") {

        coEvery {
            api.getTranslation(match { it is TranslationRequest.Movie && it.artworkId == 1L })
        } returns Translation(title = "new title for movie", description = "new description for movie")

        coEvery {
            api.getTranslation(match { it is TranslationRequest.Show && it.artworkId == 2L })
        } returns Translation(title = "new title for show", description = "new description for show")

        coEvery {
            api.getTranslation(match { it is TranslationRequest.Season && it.artworkId == 2L && it.season == 1 })
        } returns Translation(title = "new title for season", description = "new description for season")

        coEvery {
            api.getTranslation(match { it is TranslationRequest.Episode && it.artworkId == 2L && it.season == 1 && it.number == 1 })
        } returns Translation(title = "new title for episode", description = "new description for episode")

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            database.saveMedias(match { list ->
                list.size == 1 && list[0].title == "new title for movie" && list[0].description == "new description for movie"
            })
        }
        coVerify(exactly = 1) {
            database.saveArtworks(match { list ->
                list.size == 1 && list[0].title == "new title for show" && list[0].description == "new description for show"
            }, overrideLastModification = false)
        }
        coVerify(exactly = 1) {
            database.saveSeasons(match { list ->
                list.size == 1 && list[0].title == "new title for season" && list[0].description == "new description for season"
            })
        }
        coVerify(exactly = 1) {
            database.saveMedias(match { list ->
                list.size == 1 && list[0].title == "new title for episode" && list[0].description == "new description for episode"
            })
        }
    }

    test("if no translation available, no save") {

        coEvery { api.getTranslation(any()) } returns null

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { database.saveMedias(any()) }
        coVerify(exactly = 0) { database.saveArtworks(any()) }
        coVerify(exactly = 0) { database.saveSeasons(any()) }
    }

    test("if only the title is translated, keep the current description") {

        coEvery {
            api.getTranslation(match { it is TranslationRequest.Movie && it.artworkId == 1L })
        } returns Translation(title = "new title", description = null)

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            database.saveMedias(match { list ->
                list.size == 1 && list[0].title == "new title" && list[0].description == "Old Movie Desc"
            })
        }
    }

    test("if only the description is translated, keep the current title") {

        coEvery {
            api.getTranslation(match { it is TranslationRequest.Movie && it.artworkId == 1L })
        } returns Translation(title = null, description = "new description")

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            database.saveMedias(match { list ->
                list.size == 1 && list[0].title == "Old Movie Title" && list[0].description == "new description"
            })
        }
    }

})