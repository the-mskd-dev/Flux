package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.language.Language
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Behaviour of the Language change Sync kind, observed through the Catalog in database,
 * the Genres, the Sync state, the images prefetch and the TMDB calls.
 */
class LanguageChangeSyncBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    suspend fun awaitCall(endpoint: String) = withTimeout(10.seconds) {
        while (harness.tmdb.callCount(endpoint) == 0) delay(10)
    }

    test("a Language change updates Genres in the new data language without scanning sources or re-identifying files") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.fullSync()
        harness.details.flowGenres().first().map { it.name } shouldContainExactlyInAnyOrder listOf(
            "Action",
            "Science Fiction",
            "Drama",
            "Crime",
        )

        harness.settings.setDataLanguage(Language.FRENCH)
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.tmdb.resetCalls()

        // When
        harness.languageChangeSync()

        // Then
        harness.details.flowGenres().first().map { it.name } shouldContainExactlyInAnyOrder listOf(
            "Action",
            "Science-Fiction",
            "Drame",
            "Crime",
        )
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        harness.tmdb.callCount("genre/tv/list") shouldBe 1
        val matchingOrDetailsCalls = harness.tmdb.callCount("search/movie") +
            harness.tmdb.callCount("search/tv") +
            harness.tmdb.callCount("movie/{id}") +
            harness.tmdb.callCount("tv/{id}")
        matchingOrDetailsCalls shouldBe 0
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a Language change does not remove Missing files, reset user choices, move NSFW Artwork, or overwrite Sync time") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Forbidden Film (2020).mkv"),
        )
        harness.lightSync()

        harness.catalog.setArtworkCustomPoster(artworkId = 27205L, path = "/custom-poster.jpg")
        harness.catalog.setArtworkCustomBackdrop(artworkId = 27205L, path = "/custom-backdrop.jpg")
        harness.catalog.setArtworkCustomLogo(artworkId = 27205L, path = "/custom-logo.png")
        harness.identifyManually(
            artworkId = 1396L,
            target = Artwork(id = 990003L, title = "Lost Pages", type = ContentType.SHOW),
        ).getOrThrow()

        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Forbidden Film (2020).mkv"),
        )
        harness.privateFolder.setEnabled(true)
        harness.privateFolder.setIncludeNsfw(true)
        harness.user.setSyncTime(1234L)
        harness.user.setVersionCode(0)

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val inception = harness.catalog.getArtwork(artworkId = 27205L)
        inception shouldNotBe null
        inception?.customPosterPath shouldBe "/custom-poster.jpg"
        inception?.customBackdropPath shouldBe "/custom-backdrop.jpg"
        inception?.customLogoPath shouldBe "/custom-logo.png"
        harness.catalog.getArtwork(artworkId = 990003L)?.isManuallyIdentified shouldBe true
        harness.catalog.getArtwork(artworkId = 990001L)?.isPrivate shouldBe false
        harness.user.getSyncTime() shouldBe 1234L
        harness.user.getVersionCode() shouldBe 0

    }

    test("a Language change prefetches Artwork images at the end") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val prefetchesBefore = harness.prefetch.prefetchCount
        harness.tmdb.resetCalls()
        harness.tmdb.pause("genre/movie/list")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.requestLanguageChangeSync()
        awaitCall("genre/movie/list")

        // Then
        harness.prefetch.prefetchCount shouldBe prefetchesBefore

        // When
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.prefetch.prefetchCount shouldBe prefetchesBefore + 1

    }

    test("Sync progress moves monotonically during a Language change, reaches 100% only at completion, and reports non-blocking syncing state") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val statesBefore = harness.states.size

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val syncingStates = harness.states.drop(statesBefore).filterIsInstance<SyncState.Syncing>()
        syncingStates.map { it.full }.distinct() shouldBe listOf(false)
        val progresses = syncingStates.map { it.progress }
        progresses.zipWithNext().all { (previous, next) -> next >= previous } shouldBe true
        progresses.distinct().size shouldBeGreaterThan 1
        progresses.dropLast(1).all { it < 1f } shouldBe true
        progresses.last() shouldBe 1f
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("without a TMDB token, a Language change fires no TMDB lookups and completes cleanly") {

        // Given
        harness.close()
        harness = SyncHarness(tmdbToken = "")
        harness.settings.setDataLanguage(Language.FRENCH)

        // When
        harness.languageChangeSync()

        // Then
        harness.tmdb.totalCallCount shouldBe 0
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a Language change cancels a running Light sync") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.pause("search/movie")
        harness.requestLightSync()
        awaitCall("search/movie")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.requestLanguageChangeSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.catalog.getArtworks(includePrivates = true) shouldBe emptyList()
        harness.details.flowGenres().first().map { it.name } shouldContainAll listOf("Science-Fiction", "Drame")
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a Language change cancels a running Full sync") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.pause("search/movie")
        harness.requestFullSync()
        awaitCall("search/movie")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.requestLanguageChangeSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(true, false)
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.catalog.getArtworks(includePrivates = true) shouldBe emptyList()
        harness.details.flowGenres().first().map { it.name } shouldContainAll listOf("Science-Fiction", "Drame")
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a Light sync cancels a running Language change") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.tmdb.pause("genre/movie/list")
        harness.requestLanguageChangeSync()
        awaitCall("genre/movie/list")

        // When
        harness.requestLightSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")
        harness.prefetch.prefetchCount shouldBe 1
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a Full sync cancels a running Language change") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.tmdb.pause("genre/movie/list")
        harness.requestLanguageChangeSync()
        awaitCall("genre/movie/list")

        // When
        harness.requestFullSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(false, true)
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")
        harness.prefetch.prefetchCount shouldBe 1
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("changing data language updates translations, Genres, and Logos, prefetches images, and is idempotent when repeated") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.fullSync()
        val prefetchesBefore = harness.prefetch.prefetchCount

        // When
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        harness.catalog.getMovie(artworkId = 990001L)!!.title shouldBe "Film Interdit"
        harness.catalog.getArtwork(artworkId = 990001L)!!.title shouldBe "Film Interdit"
        harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }.title shouldBe "Saison 1"
        harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }.title shouldBe "Chute libre"
        harness.details.flowGenres().first().map { it.name } shouldContainAll listOf("Science-Fiction", "Drame")
        harness.catalog.getArtwork(artworkId = 27205L)!!.logoPath shouldBe "/inception-fr-best.png"
        harness.prefetch.prefetchCount shouldBe prefetchesBefore + 1
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

        // When: repeating the same data language
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        harness.catalog.getMovie(artworkId = 990001L)!!.title shouldBe "Film Interdit"
        harness.catalog.getArtwork(artworkId = 990001L)!!.title shouldBe "Film Interdit"
        harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }.title shouldBe "Saison 1"
        harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }.title shouldBe "Chute libre"
        harness.details.flowGenres().first().map { it.name } shouldContainAll listOf("Science-Fiction", "Drame")
        harness.catalog.getArtwork(artworkId = 27205L)!!.logoPath shouldBe "/inception-fr-best.png"
        harness.prefetch.prefetchCount shouldBe prefetchesBefore + 2
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

})
