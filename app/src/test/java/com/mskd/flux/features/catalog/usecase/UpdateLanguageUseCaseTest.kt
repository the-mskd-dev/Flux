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
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCaseImpl
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

    lateinit var remoteRepository: ArtworkRemoteRepository
    lateinit var database: DatabaseRepository
    lateinit var settings: SettingsDataStore
    lateinit var coordinator: CatalogSyncCoordinator
    lateinit var useCase: UpdateLanguageUseCaseImpl

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

    beforeTest {
        remoteRepository = mockk()
        database = mockk(relaxed = true)
        settings = mockk(relaxed = true)
        coordinator = FakeCatalogSyncCoordinator(scope = testScope)

        coEvery { settings.getDataLanguage() } returns Locale.FRENCH
        coEvery { database.getMovies() } returns listOf(movie)
        coEvery { database.getArtworks() } returns listOf(showArtwork)
        coEvery { database.getSeasons() } returns listOf(season)
        coEvery { database.getEpisodes() } returns listOf(episode)

        useCase = UpdateLanguageUseCaseImpl(
            remoteRepository = remoteRepository,
            database = database,
            settings = settings,
            coordinator = coordinator,
            dispatcher = testDispatcher
        )
    }

    test("récupère les traductions pour movie/show/season/episode et les sauvegarde") {

        coEvery {
            remoteRepository.translate(match { it is TranslationRequest.Movie && it.artworkId == 1L })
        } returns Translation(title = "Titre Traduit", description = "Description Traduite")

        coEvery {
            remoteRepository.translate(match { it is TranslationRequest.Show && it.artworkId == 2L })
        } returns Translation(title = "Serie Traduite", description = "Resume Traduit")

        coEvery {
            remoteRepository.translate(match { it is TranslationRequest.Season && it.artworkId == 2L && it.season == 1 })
        } returns Translation(title = "Saison Traduite", description = "Resume Saison Traduit")

        coEvery {
            remoteRepository.translate(match { it is TranslationRequest.Episode && it.artworkId == 2L && it.season == 1 && it.number == 1 })
        } returns Translation(title = "Episode Traduit", description = "Resume Episode Traduit")

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

    test("aucune traduction disponible conserve les titres/descriptions existants et ne sauvegarde rien") {

        coEvery { remoteRepository.translate(any()) } returns null

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { database.saveMovies(any()) }
        coVerify(exactly = 0) { database.saveArtworks(any()) }
        coVerify(exactly = 0) { database.saveSeasons(any()) }
        coVerify(exactly = 0) { database.saveEpisodes(any()) }
    }

    test("une traduction partielle (titre seul) conserve la description existante") {

        coEvery {
            remoteRepository.translate(match { it is TranslationRequest.Movie && it.artworkId == 1L })
        } returns Translation(title = "Titre Traduit", description = null)

        useCase()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            database.saveMovies(match { list ->
                list.size == 1 && list[0].title == "Titre Traduit" && list[0].description == "Old Movie Desc"
            })
        }
    }

})