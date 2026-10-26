package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Behaviour of the Synchro, observed through the Catalogue in database, the successive Synchro
 * states and the TMDB calls. See docs/specs/sync-catalog.md.
 */
class SyncCatalogBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    test("the first Synchro fetches and saves every file of the Sources") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S01E02.mkv"),
        )

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")
        harness.catalog.getSeasons().map { it.title } shouldContainExactlyInAnyOrder listOf("Season 1")
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Pilot", "Cat's in the Bag...")
        harness.catalog.getMedias().filterIsInstance<Movie>().single().duration shouldBe 148
        harness.catalog.getMedias().filterIsInstance<Episode>().map { it.artworkId }.distinct() shouldBe listOf(1396L)

    }

    test("a Synchro légère only fetches the new files") {

        // Given
        val inception = harness.deviceFile("Inception (2010).mkv")
        harness.deviceStorage.availableFiles = listOf(inception)
        harness.lightSync()

        harness.deviceStorage.availableFiles = listOf(inception, harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.tmdb.resetCalls()

        // When
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/movie") shouldBe 0
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.tmdb.callCount("search/tv") shouldBe 1
        harness.tmdb.callCount("tv/{id}/season/{season}") shouldBe 1
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Pilot")

    }

    test("a Synchro complète fetches every file again and refreshes the genres") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        harness.tmdb.resetCalls()

        // When
        harness.fullSync()

        // Then
        harness.tmdb.callCount("search/movie") shouldBe 1
        harness.tmdb.callCount("movie/{id}") shouldBe 1
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        harness.tmdb.callCount("genre/tv/list") shouldBe 1
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")

    }

    test("a file that TMDB cannot identify is saved as a Fichier non reconnu and is not fetched again") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Holidays in Brittany.mkv"))

        // When
        harness.lightSync()
        harness.tmdb.resetCalls()
        harness.lightSync()

        // Then
        val media = harness.catalog.getMedias().single()
        media.shouldBeInstanceOf<Episode>()
        media.artworkId shouldBe Artwork.UNKNOWN_ID
        media.file.name shouldBe "Holidays in Brittany.mkv"
        harness.tmdb.callCount("search/{type}") shouldBe 0

    }

    test("the state goes from Idle to Syncing and back to Idle") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))

        // When
        harness.lightSync()

        // Then
        harness.states.first() shouldBe SyncState.Idle
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(false)
        harness.states.last() shouldBe SyncState.Idle

    }

    test("a Synchro légère requested during a Synchro complète is ignored") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.pause()
        harness.requestFullSync()

        // When
        harness.requestLightSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(true)
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")

    }

    test("a Synchro complète requested during a Synchro légère replaces it") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.pause()
        harness.requestLightSync()

        // When
        harness.requestFullSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(false, true)
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        // The cancelled Synchro légère never got further than its search
        harness.tmdb.callCount("movie/{id}") shouldBe 1
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")

    }

    test("an Artwork privé stays private after a Synchro complète") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkPrivate(artworkId = 27205L, isPrivate = true)

        // When
        harness.fullSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).associate { it.title to it.isPrivate } shouldBe mapOf(
            "Inception" to true,
            "Breaking Bad" to false,
        )

    }

    test("NSFW Artworks are moved to the private folder when it includes them") {

        // Given
        harness.privateFolder.setEnabled(true)
        harness.privateFolder.setIncludeNsfw(true)
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Forbidden Film (2020).mkv"),
        )

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).associate { it.title to it.isPrivate } shouldBe mapOf(
            "Inception" to false,
            "Forbidden Film" to true,
        )

    }

})
