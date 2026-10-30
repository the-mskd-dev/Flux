package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

/**
 * Behaviour of the new files of a Synchro légère towards an Identification manuelle, observed through the Catalogue in database.
 * See .scratch/identification-manuelle/specs.md and docs/adr/0001-identification-manuelle-par-titre-de-fichier.md.
 */
class NewFilesManualIdentificationBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    val breakingBad = 1396L
    val lostPagesS01E01 = 70011L
    val lostPagesS01E02 = 70012L
    val lostPages = Artwork(id = 990003, title = "Lost Pages", type = ContentType.SHOW)

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    /** Breaking Bad S01E01 synchronised, then identified manually as Lost Pages. */
    suspend fun givenManuallyIdentifiedShow() {
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.lightSync()
        harness.identifyManually(artworkId = breakingBad, target = lostPages).getOrThrow()
        harness.tmdb.resetCalls()
    }

    test("a new episode of the same file-name title joins the manually identified Show without a name search") {

        // Given
        givenManuallyIdentifiedShow()

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Breaking Bad S01E02.mkv")
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/tv") shouldBe 0
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe lostPages.id
        artwork.isManuallyIdentified shouldBe true
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name to it.id } shouldContainExactlyInAnyOrder listOf(
            "Breaking Bad S01E01.mkv" to lostPagesS01E01,
            "Breaking Bad S01E02.mkv" to lostPagesS01E02,
        )

    }

    test("a TMDB ID in the folder name wins over the Identification manuelle") {

        // Given
        givenManuallyIdentifiedShow()

        // When
        harness.deviceStorage.availableFiles += UserFile(
            name = "Breaking Bad S02E01.mkv",
            path = "${SyncHarness.DEVICE_ROOT}Breaking Bad S02E01.mkv",
            realPath = "Breaking Bad {tmdb-$breakingBad}/Breaking Bad S02E01.mkv",
            source = FileSource.LOCAL,
        )
        harness.lightSync()

        // Then
        harness.catalog.getEpisodes(artworkId = breakingBad).map { it.file.name } shouldBe listOf("Breaking Bad S02E01.mkv")
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Breaking Bad S01E01.mkv")
        harness.catalog.getArtwork(artworkId = breakingBad)?.isManuallyIdentified shouldBe false
        harness.catalog.getArtwork(artworkId = lostPages.id)?.isManuallyIdentified shouldBe true

    }

    test("a new file of another file-name title is identified by name search") {

        // Given
        givenManuallyIdentifiedShow()

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Lost Pages S02E01.mkv")
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/tv") shouldBe 1
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Breaking Bad S01E01.mkv",
            "Lost Pages S02E01.mkv",
        )
        harness.catalog.getArtwork(artworkId = lostPages.id)?.isManuallyIdentified shouldBe true

    }

    test("a new Movie file of the same file-name title as the manually identified Show is identified by name search") {

        // Given
        givenManuallyIdentifiedShow()

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Breaking Bad.mkv")
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/movie") shouldBe 1
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Breaking Bad S01E01.mkv")
        harness.catalog.getMovie(artworkId = lostPages.id) shouldBe null

    }

    test("when every file of the manually identified Show disappears, the choice is forgotten") {

        // Given
        givenManuallyIdentifiedShow()
        harness.deviceStorage.availableFiles = emptyList()
        harness.lightSync()

        // When
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E02.mkv"))
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/tv") shouldBe 1
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe breakingBad
        artwork.isManuallyIdentified shouldBe false

    }

})
