package com.mskd.flux.features.catalog.sync

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Image personnalisée of an Artwork, observed through the Catalogue in database.
 * See .scratch/poster-selection/specs.md.
 */
class CustomImageBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    suspend fun artworkOf(title: String) =
        harness.catalog.getArtworks(includePrivates = true).single { it.title == title }

    test("a custom Backdrop set on an Artwork is read back from the Catalog, the rest of the Artwork untouched") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val before = artworkOf("Inception")

        // When
        harness.catalog.setArtworkCustomBackdrop(artworkId = before.id, path = "/chosen-backdrop.jpg")

        // Then
        val after = artworkOf("Inception")
        after.customBackdropPath shouldBe "/chosen-backdrop.jpg"
        after.customPosterPath shouldBe null
        after.copy(customBackdropPath = null) shouldBe before

    }

    test("a Synchro légère keeps the custom Backdrop and custom Poster of every Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val inception = artworkOf("Inception")
        harness.catalog.setArtworkCustomBackdrop(artworkId = inception.id, path = "/chosen-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = inception.id, path = "/chosen-poster.jpg")

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Breaking Bad S01E01.mkv")
        harness.lightSync()

        // Then
        val after = artworkOf("Inception")
        after.customBackdropPath shouldBe "/chosen-backdrop.jpg"
        after.customPosterPath shouldBe "/chosen-poster.jpg"

    }

    test("a Synchro légère that adds a new Episode to a Show keeps its Image personnalisée") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.lightSync()
        val show = artworkOf("Breaking Bad")
        harness.catalog.setArtworkCustomBackdrop(artworkId = show.id, path = "/chosen-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = show.id, path = "/chosen-poster.jpg")

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Breaking Bad S01E02.mkv")
        harness.lightSync()

        // Then
        harness.catalog.getEpisodes(artworkId = show.id).size shouldBe 2
        val after = artworkOf("Breaking Bad")
        after.customBackdropPath shouldBe "/chosen-backdrop.jpg"
        after.customPosterPath shouldBe "/chosen-poster.jpg"

    }

    test("a Synchro complète clears the Image personnalisée of every Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        val inception = artworkOf("Inception")
        val show = artworkOf("Breaking Bad")
        harness.catalog.setArtworkCustomBackdrop(artworkId = inception.id, path = "/inception-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = inception.id, path = "/inception-poster.jpg")
        harness.catalog.setArtworkCustomBackdrop(artworkId = show.id, path = "/show-backdrop.jpg")

        // When
        harness.fullSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).forEach { artwork ->
            artwork.customBackdropPath shouldBe null
            artwork.customPosterPath shouldBe null
        }
        artworkOf("Inception").imagePath shouldBe inception.imagePath

    }

})
