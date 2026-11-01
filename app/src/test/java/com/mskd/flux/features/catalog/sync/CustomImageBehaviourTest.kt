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

})
