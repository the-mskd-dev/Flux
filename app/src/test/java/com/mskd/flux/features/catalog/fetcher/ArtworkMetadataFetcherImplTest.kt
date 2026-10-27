package com.mskd.flux.features.catalog.fetcher

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkMetadataFetcherImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class ArtworkMetadataFetcherImplTest : FunSpec({

    val movieFile = UserFile(name = "Dune.mkv", path = "path/dune")
    val episodeFile = UserFile(name = "Game of Thrones S01E01.mkv", path = "path/got")

    val idArtwork = Artwork(id = 438631, title = "Dune (by id)", type = ContentType.MOVIE)
    val nameArtwork = Artwork(id = 1, title = "Dune (by name)", type = ContentType.MOVIE)

    test("prioritizes the TMDB ID if it exists, before the file name") {
        runTest {
            val api = mockk<ApiRepository>()
            coEvery { api.getArtworkById(438631, ContentType.MOVIE) } returns idArtwork
            val fetcher = ArtworkMetadataFetcherImpl(api, StandardTestDispatcher(testScheduler))

            val result = fetcher.fetch(
                folders = listOf(CatalogFolder(title = "dune", files = listOf(movieFile), tmdbId = 438631)),
                onProgress = {}
            )

            result.found.single().artwork shouldBe idArtwork
            coVerify(exactly = 1) { api.getArtworkById(438631, ContentType.MOVIE) }
            coVerify(exactly = 0) { api.getArtwork(any()) }
        }
    }

    test("an episode folder with a TMDB ID is fetched as a show") {
        runTest {
            val showArtwork = Artwork(id = 1399, title = "Game of Thrones", type = ContentType.SHOW)
            val api = mockk<ApiRepository>()
            coEvery { api.getArtworkById(1399, ContentType.SHOW) } returns showArtwork
            val fetcher = ArtworkMetadataFetcherImpl(api, StandardTestDispatcher(testScheduler))

            val result = fetcher.fetch(
                folders = listOf(CatalogFolder(title = "game of thrones", files = listOf(episodeFile), tmdbId = 1399)),
                onProgress = {}
            )

            result.found.single().artwork shouldBe showArtwork
            coVerify(exactly = 1) { api.getArtworkById(1399, ContentType.SHOW) }
            coVerify(exactly = 0) { api.getArtwork(any()) }
        }
    }

    test("falls back to the file name if the TMDB ID is not found") {
        runTest {
            val api = mockk<ApiRepository>()
            coEvery { api.getArtworkById(any(), any()) } returns null
            coEvery { api.getArtwork(movieFile) } returns nameArtwork
            val fetcher = ArtworkMetadataFetcherImpl(api, StandardTestDispatcher(testScheduler))

            val result = fetcher.fetch(
                folders = listOf(CatalogFolder(title = "dune", files = listOf(movieFile), tmdbId = 438631)),
                onProgress = {}
            )

            result.found.single().artwork shouldBe nameArtwork
            coVerify(exactly = 1) { api.getArtworkById(438631, ContentType.MOVIE) }
            coVerify(exactly = 1) { api.getArtwork(movieFile) }
        }
    }

    test("falls back to the file name if the TMDB ID lookup returns an unknown artwork") {
        runTest {
            val api = mockk<ApiRepository>()
            coEvery { api.getArtworkById(any(), any()) } returns Artwork.UNKNOWN
            coEvery { api.getArtwork(movieFile) } returns nameArtwork
            val fetcher = ArtworkMetadataFetcherImpl(api, StandardTestDispatcher(testScheduler))

            val result = fetcher.fetch(
                folders = listOf(CatalogFolder(title = "dune", files = listOf(movieFile), tmdbId = 438631)),
                onProgress = {}
            )

            result.found.single().artwork shouldBe nameArtwork
        }
    }

    test("falls back to the file name if the TMDB ID lookup throws, and reports progress once per folder") {
        runTest {
            val api = mockk<ApiRepository>()
            coEvery { api.getArtworkById(any(), any()) } throws RuntimeException("network")
            coEvery { api.getArtwork(movieFile) } returns nameArtwork
            val fetcher = ArtworkMetadataFetcherImpl(api, StandardTestDispatcher(testScheduler))
            var progress = 0

            val result = fetcher.fetch(
                folders = listOf(CatalogFolder(title = "dune", files = listOf(movieFile), tmdbId = 438631)),
                onProgress = { progress++ }
            )

            result.found.single().artwork shouldBe nameArtwork
            coVerify(exactly = 1) { api.getArtwork(movieFile) }
            progress shouldBe 1
        }
    }

    test("a folder without TMDB ID is fetched by file name only") {
        runTest {
            val api = mockk<ApiRepository>()
            coEvery { api.getArtwork(movieFile) } returns nameArtwork
            val fetcher = ArtworkMetadataFetcherImpl(api, StandardTestDispatcher(testScheduler))

            val result = fetcher.fetch(
                folders = listOf(CatalogFolder(title = "dune", files = listOf(movieFile))),
                onProgress = {}
            )

            result.found.single().artwork shouldBe nameArtwork
            coVerify(exactly = 1) { api.getArtwork(movieFile) }
            coVerify(exactly = 0) { api.getArtworkById(any(), any()) }
        }
    }

})
