package com.mskd.flux.features.catalog.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.remote.dto.TranslationsDto
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCaseImpl
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateLanguageUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var tmdb: TmdbDataSource
    lateinit var database: DatabaseRepository
    lateinit var settings: SettingsDataStore
    lateinit var coordinator: CatalogSyncCoordinator
    lateinit var useCase: UpdateLanguageUseCaseImpl

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    beforeTest {
        tmdb = mockk(relaxed = true)
        database = mockk(relaxed = true)
        settings = mockk(relaxed = true)
        coordinator = CatalogSyncCoordinator(scope = testScope)

        useCase = UpdateLanguageUseCaseImpl(
            tmdb = tmdb,
            database = database,
            settings = settings,
            coordinator = coordinator,
            dispatcher = testDispatcher
        )
    }

    test("update language fetches translations and saves them") {
        val locale = Locale.FRENCH
        coEvery { settings.getDataLanguage() } returns locale

        val movie = Movie(
            artworkId = 1L,
            title = "Old Movie Title",
            description = "Old Movie Desc",
            releaseDateString = "",
            voteAverage = 0f,
            voteCount = 0,
            duration = 120,
            file = mockk()
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
            file = mockk()
        )

        coEvery { database.getMovies() } returns listOf(movie)
        coEvery { database.getArtworks() } returns listOf(showArtwork)
        coEvery { database.getSeasons() } returns listOf(season)
        coEvery { database.getEpisodes() } returns listOf(episode)

        // Mock TMDB Translation DTOs
        val movieTranslation = TranslationsDto.Translation(
            language = "fr",
            country = "FR",
            name = "French Movie Name",
            englishName = "French Movie Name",
            data = TranslationsDto.Data(name = "Titre Traduit", overview = "Description Traduite")
        )
        coEvery { tmdb.getTmdbTranslation(match { it is TranslationsDto.Request.Movie && it.artworkId == 1L }) } returns movieTranslation

        val showTranslation = TranslationsDto.Translation(
            language = "fr",
            country = "FR",
            name = "French Show Name",
            englishName = "French Show Name",
            data = TranslationsDto.Data(name = "Serie Traduite", overview = "Resume Traduit")
        )
        coEvery { tmdb.getTmdbTranslation(match { it is TranslationsDto.Request.Show && it.artworkId == 2L }) } returns showTranslation

        val seasonTranslation = TranslationsDto.Translation(
            language = "fr",
            country = "FR",
            name = "French Season Name",
            englishName = "French Season Name",
            data = TranslationsDto.Data(name = "Saison Traduite", overview = "Resume Saison Traduit")
        )
        coEvery { tmdb.getTmdbTranslation(match { it is TranslationsDto.Request.Season && it.artworkId == 2L && it.season == 1 }) } returns seasonTranslation

        val episodeTranslation = TranslationsDto.Translation(
            language = "fr",
            country = "FR",
            name = "French Episode Name",
            englishName = "French Episode Name",
            data = TranslationsDto.Data(name = "Episode Traduit", overview = "Resume Episode Traduit")
        )
        coEvery { tmdb.getTmdbTranslation(match { it is TranslationsDto.Request.Episode && it.artworkId == 2L && it.season == 1 && it.number == 1 }) } returns episodeTranslation

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            database.saveMovies(match { list ->
                list.size == 1 && list[0].title == "Titre Traduit" && list[0].description == "Description Traduite"
            })
        }
        coVerify(exactly = 1) {
            database.saveArtworks(match { list ->
                list.size == 1 && list[0].title == "Serie Traduite" && list[0].description == "Resume Traduit"
            })
        }
        coVerify(exactly = 1) {
            database.saveSeasons(match { list ->
                list.size == 1 && list[0].title == "Saison Traduite" && list[0].description == "Resume Saison Traduit"
            })
        }
        coVerify(exactly = 1) {
            database.saveEpisodes(match { list ->
                list.size == 1 && list[0].title == "Episode Traduit" && list[0].description == "Resume Episode Traduit"
            })
        }
    }

})
