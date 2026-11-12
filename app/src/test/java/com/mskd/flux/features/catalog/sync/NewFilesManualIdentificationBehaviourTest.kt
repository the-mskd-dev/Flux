package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

/**
 * Behaviour of the new files of a Light sync towards a Manual identification, observed through the Catalog in database.
 * See .scratch/manual-identification/specs.md and docs/adr/0001-manual-identification-by-file-name-title.md.
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

    test("a new episode with a TMDB ID in its folder name follows the Manual identification of its file-name title") {

        // Given
        givenManuallyIdentifiedShow()

        // When
        harness.deviceStorage.availableFiles += UserFile(
            name = "Breaking Bad S01E02.mkv",
            path = "${SyncHarness.DEVICE_ROOT}Breaking Bad S01E02.mkv",
            realPath = "Breaking Bad {tmdb-$breakingBad}/Breaking Bad S01E02.mkv",
            source = FileSource.LOCAL,
        )
        harness.lightSync()

        // Then
        harness.tmdb.callCount("tv/$breakingBad") shouldBe 0
        harness.tmdb.callCount("search/tv") shouldBe 0
        harness.catalog.getArtwork(artworkId = breakingBad) shouldBe null
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name to it.id } shouldContainExactlyInAnyOrder listOf(
            "Breaking Bad S01E01.mkv" to lostPagesS01E01,
            "Breaking Bad S01E02.mkv" to lostPagesS01E02,
        )
        harness.catalog.getArtwork(artworkId = lostPages.id)?.isManuallyIdentified shouldBe true

    }

    test("a new file with a TMDB ID in its name and another file-name title gets the Artwork of that TMDB ID") {

        // Given
        givenManuallyIdentifiedShow()

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Some Other Name S02E01 {tmdb-$breakingBad}.mkv")
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/tv") shouldBe 0
        harness.catalog.getEpisodes(artworkId = breakingBad).map { it.file.name } shouldBe listOf("Some Other Name S02E01 {tmdb-$breakingBad}.mkv")
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Breaking Bad S01E01.mkv")
        harness.catalog.getArtwork(artworkId = breakingBad)?.isManuallyIdentified shouldBe false

    }

    listOf(
        listOf("Breaking Bad S01E02 {tmdb-1396}.mkv", "Some Other Name S02E01 {tmdb-1396}.mkv"),
        listOf("Some Other Name S02E01 {tmdb-1396}.mkv", "Breaking Bad S01E02 {tmdb-1396}.mkv"),
    ).forEach { names ->
        test("new files of the same TMDB ID with mixed file-name titles all get the Artwork of that TMDB ID, whatever their order: $names") {

            // Given
            givenManuallyIdentifiedShow()

            // When
            harness.deviceStorage.availableFiles += names.map { harness.deviceFile(it) }
            harness.lightSync()

            // Then
            harness.catalog.getEpisodes(artworkId = breakingBad).map { it.file.name } shouldContainExactlyInAnyOrder names
            harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Breaking Bad S01E01.mkv")

        }
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
