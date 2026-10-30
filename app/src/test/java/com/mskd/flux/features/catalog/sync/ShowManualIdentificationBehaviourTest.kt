package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.data.model.HistoryEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

/**
 * Behaviour of the Identification manuelle of a Show, observed through the Catalogue in database.
 * See .scratch/identification-manuelle/specs.md.
 */
class ShowManualIdentificationBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    val breakingBad = 1396L
    val pilot = 62085L
    val breakingBadS02E01 = 62092L
    val lostPagesS01E01 = 70011L
    val lostPagesS02E01 = 70021L
    val lostPages = Artwork(id = 990003, title = "Lost Pages", type = ContentType.SHOW)

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    test("every episode of the Show is re-identified to the chosen Show, with its Seasons, and the old Show is deleted") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S01E02.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
        )
        harness.lightSync()

        // When
        val result = harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        result.getOrNull() shouldBe lostPages.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe lostPages.id
        artwork.isManuallyIdentified shouldBe true
        harness.catalog.getSeasons().map { it.artworkId to it.season } shouldContainExactlyInAnyOrder listOf(lostPages.id to 1, lostPages.id to 2)
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name to it.title } shouldContainExactlyInAnyOrder listOf(
            "Breaking Bad S01E01.mkv" to "Le début",
            "Breaking Bad S01E02.mkv" to "The Missing Page",
            "Breaking Bad S02E01.mkv" to "The Next Chapter",
        )
        harness.catalog.getMedias().filterIsInstance<Episode>().all { it.artworkId == lostPages.id } shouldBe true

    }

    test("an episode of a Season that the chosen Show does not have goes to the Unknown Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S03E01.mkv"),
        )
        harness.lightSync()

        // When
        val result = harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        result.getOrNull() shouldBe lostPages.id
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldContainExactlyInAnyOrder listOf(lostPages.id, Artwork.UNKNOWN_ID)
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Breaking Bad S01E01.mkv")
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("Breaking Bad S03E01.mkv")
        harness.catalog.getSeasons().map { it.artworkId to it.season } shouldBe listOf(lostPages.id to 1)

    }

    test("the episodes of a Season that fails are Fichiers en échec: they stay where they were, with their Status, and the others move") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
        )
        harness.lightSync()
        val failing = harness.catalog.getEpisodes(artworkId = breakingBad).single { it.season == 2 }
        harness.catalog.saveMedias(listOf(failing.copy(status = Status.WATCHED)))
        harness.tmdb.failEndpoint("tv/${lostPages.id}/season/2")

        // When
        val result = harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        result.getOrNull() shouldBe lostPages.id
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Breaking Bad S01E01.mkv")
        val stayed = harness.catalog.getEpisodes(artworkId = breakingBad).single()
        stayed.file.name shouldBe "Breaking Bad S02E01.mkv"
        stayed.status shouldBe Status.WATCHED
        harness.catalog.getUnknownMedias() shouldBe emptyList()

    }

    test("when no Season of the chosen Show can be fetched, nothing changes") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
        )
        harness.lightSync()
        harness.history.upsert(HistoryEntity(artworkId = breakingBad, mediaId = pilot, timestamp = 1000L))
        harness.tmdb.cutNetwork()

        // When
        val result = harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        result.isFailure shouldBe true
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe breakingBad
        artwork.isManuallyIdentified shouldBe false
        harness.catalog.getEpisodes(artworkId = breakingBad).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Breaking Bad S01E01.mkv",
            "Breaking Bad S02E01.mkv",
        )
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = breakingBad, mediaId = pilot, timestamp = 1000L))

    }

    test("the Status, the Progress, the history entry and the private flag follow the episodes to the chosen Show") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
        )
        harness.lightSync()
        val episode = harness.catalog.getEpisodes(artworkId = breakingBad).single { it.season == 2 }
        harness.catalog.saveMedias(listOf(episode.copy(status = Status.IS_WATCHING, currentTime = 1234L)))
        harness.history.upsert(HistoryEntity(artworkId = breakingBad, mediaId = episode.id, timestamp = 1000L))
        harness.catalog.setArtworkPrivate(artworkId = breakingBad, isPrivate = true)

        // When
        harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        val moved = harness.catalog.getEpisodes(artworkId = lostPages.id).single { it.season == 2 }
        moved.id shouldBe lostPagesS02E01
        moved.status shouldBe Status.IS_WATCHING
        moved.currentTime shouldBe 1234L
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS02E01, timestamp = 1000L))
        harness.catalog.getArtwork(artworkId = lostPages.id)?.isPrivate shouldBe true

    }

    test("choosing a Show of the Catalogue merges the Seasons and episodes into it, keeping the most recent history entry") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Lost Pages S01E01.mkv"),
            harness.deviceFile("Breaking Bad S01E02.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
        )
        harness.lightSync()
        harness.history.upsert(HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS01E01, timestamp = 1000L))
        harness.history.upsert(HistoryEntity(artworkId = breakingBad, mediaId = breakingBadS02E01, timestamp = 2000L))

        // When
        harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe lostPages.id
        artwork.isManuallyIdentified shouldBe true
        harness.catalog.getSeasons().map { it.artworkId to it.season } shouldContainExactlyInAnyOrder listOf(lostPages.id to 1, lostPages.id to 2)
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Lost Pages S01E01.mkv",
            "Breaking Bad S01E02.mkv",
            "Breaking Bad S02E01.mkv",
        )
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS02E01, timestamp = 2000L))

    }

    test("on a merge, a more recent history entry of the chosen Show is kept") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Lost Pages S01E01.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
        )
        harness.lightSync()
        harness.history.upsert(HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS01E01, timestamp = 3000L))
        harness.history.upsert(HistoryEntity(artworkId = breakingBad, mediaId = breakingBadS02E01, timestamp = 2000L))

        // When
        harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS01E01, timestamp = 3000L))

    }

    test("when no episode exists in the chosen Show, nothing changes") {

        // Given: Lost Pages has a Season 1, without a third episode, and no Season 3
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E03.mkv"),
            harness.deviceFile("Breaking Bad S03E01.mkv"),
        )
        harness.lightSync()

        // When
        val result = harness.identifyManually(artworkId = breakingBad, target = lostPages)

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(breakingBad)
        harness.catalog.getEpisodes(artworkId = breakingBad).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Breaking Bad S01E03.mkv",
            "Breaking Bad S03E01.mkv",
        )
        harness.catalog.getUnknownMedias() shouldBe emptyList()

    }

})
