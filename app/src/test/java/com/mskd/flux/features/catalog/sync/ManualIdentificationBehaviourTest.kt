package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.data.model.HistoryEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Behaviour of the Identification manuelle of a Movie, observed through the Catalogue in database.
 * See .scratch/identification-manuelle/specs.md.
 */
class ManualIdentificationBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    val inception = 27205L
    val forgottenTale = Artwork(id = 990002, title = "Forgotten Tale", type = ContentType.MOVIE)

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    test("the Movie is re-identified to the chosen Artwork, marked as manually identified, and the old Artwork is deleted") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()

        // When
        val result = harness.identifyManually(artworkId = inception, target = forgottenTale)

        // Then
        result.getOrNull() shouldBe forgottenTale.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe forgottenTale.id
        artwork.title shouldBe "Forgotten Tale"
        artwork.isManuallyIdentified shouldBe true
        val movie = harness.catalog.getMedias().single() as Movie
        movie.artworkId shouldBe forgottenTale.id
        movie.duration shouldBe 95
        movie.file.name shouldBe "Inception (2010).mkv"

    }

    test("the Status, the Progress and the history entry of the Movie follow it to the chosen Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val movie = harness.catalog.getMovie(artworkId = inception)!!
        harness.catalog.saveMedias(listOf(movie.copy(status = Status.IS_WATCHING, currentTime = 1234L)))
        harness.history.upsert(HistoryEntity(artworkId = inception, mediaId = inception, timestamp = 1000L))

        // When
        harness.identifyManually(artworkId = inception, target = forgottenTale)

        // Then
        val moved = harness.catalog.getMovie(artworkId = forgottenTale.id)!!
        moved.status shouldBe Status.IS_WATCHING
        moved.currentTime shouldBe 1234L
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 1000L))

    }

    test("the result is Private when the source Artwork was Private") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        harness.catalog.setArtworkPrivate(artworkId = inception, isPrivate = true)

        // When
        harness.identifyManually(artworkId = inception, target = forgottenTale)

        // Then
        harness.catalog.getArtwork(artworkId = forgottenTale.id)?.isPrivate shouldBe true

    }

    test("choosing an Artwork of the Catalogue merges into it, Private when it was, keeping its more recent history entry") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Forgotten Tale (2015).mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkPrivate(artworkId = forgottenTale.id, isPrivate = true)
        harness.history.upsert(HistoryEntity(artworkId = inception, mediaId = inception, timestamp = 1000L))
        harness.history.upsert(HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 2000L))

        // When
        harness.identifyManually(artworkId = inception, target = forgottenTale)

        // Then
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe forgottenTale.id
        artwork.isPrivate shouldBe true
        artwork.isManuallyIdentified shouldBe true
        harness.catalog.getMedias().map { it.file.name } shouldContainExactlyInAnyOrder listOf("Inception (2010).mkv")
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 2000L))

    }

    test("on a merge, the history entry of the source Artwork replaces an older one") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Forgotten Tale (2015).mkv"),
        )
        harness.lightSync()
        harness.history.upsert(HistoryEntity(artworkId = inception, mediaId = inception, timestamp = 3000L))
        harness.history.upsert(HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 2000L))

        // When
        harness.identifyManually(artworkId = inception, target = forgottenTale)

        // Then
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 3000L))

    }

    test("when the chosen Artwork cannot be fetched, nothing changes") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val movie = harness.catalog.getMovie(artworkId = inception)!!
        harness.catalog.saveMedias(listOf(movie.copy(status = Status.WATCHED)))
        harness.history.upsert(HistoryEntity(artworkId = inception, mediaId = inception, timestamp = 1000L))
        harness.tmdb.cutNetwork()

        // When
        val result = harness.identifyManually(artworkId = inception, target = forgottenTale)

        // Then
        result.isFailure shouldBe true
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe inception
        artwork.isManuallyIdentified shouldBe false
        harness.catalog.getMovie(artworkId = inception)?.status shouldBe Status.WATCHED
        harness.history.getAll() shouldBe listOf(HistoryEntity(artworkId = inception, mediaId = inception, timestamp = 1000L))

    }

    test("the current Artwork cannot be chosen") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()

        // When
        val result = harness.identifyManually(artworkId = inception, target = Artwork(id = inception, type = ContentType.MOVIE))

        // Then
        result.exceptionOrNull().shouldBeInstanceOf<IllegalStateException>()
        harness.catalog.getArtwork(artworkId = inception)?.isManuallyIdentified shouldBe false

    }

})
