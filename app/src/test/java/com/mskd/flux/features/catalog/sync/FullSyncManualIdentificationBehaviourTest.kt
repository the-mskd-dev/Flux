package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Status
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Behaviour of the Full sync towards the Manual identifications, observed through the Catalog in database.
 * See .scratch/manual-identification/specs.md.
 */
class FullSyncManualIdentificationBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    val breakingBad = 1396L
    val lostPages = Artwork(id = 990003, title = "Lost Pages", type = ContentType.SHOW)

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    test("a manually identified Show goes back to its name-search Artwork, with its Status and Progress kept") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.lightSync()
        harness.identifyManually(artworkId = breakingBad, target = lostPages).getOrThrow()
        val episode = harness.catalog.getEpisodes(artworkId = lostPages.id).single()
        harness.catalog.saveMedias(listOf(episode.copy(status = Status.IS_WATCHING, currentTime = 1234L)))

        // When
        harness.fullSync()

        // Then
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe breakingBad
        artwork.isManuallyIdentified shouldBe false
        val moved = harness.catalog.getEpisodes(artworkId = breakingBad).single()
        moved.file.name shouldBe "Breaking Bad S01E01.mkv"
        moved.status shouldBe Status.IS_WATCHING
        moved.currentTime shouldBe 1234L

    }

    test("a manually identified file with a TMDB ID in its name gets back the Artwork of its TMDB ID after a Full sync") {

        // Given
        val fileName = "Breaking Bad S01E01 {tmdb-$breakingBad}.mkv"
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile(fileName))
        harness.lightSync()
        harness.identifyManually(artworkId = breakingBad, target = lostPages).getOrThrow()
        harness.lightSync()
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf(fileName)
        harness.tmdb.resetCalls()

        // When
        harness.fullSync()

        // Then
        harness.tmdb.callCount("search/tv") shouldBe 0
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe breakingBad
        artwork.isManuallyIdentified shouldBe false
        harness.catalog.getEpisodes(artworkId = breakingBad).map { it.file.name } shouldBe listOf(fileName)

    }

})
