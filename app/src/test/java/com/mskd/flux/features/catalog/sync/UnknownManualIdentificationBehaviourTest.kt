package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.identification.domain.model.IdentificationSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

/**
 * Behaviour of the Manual identification of Unrecognized files (Unknown Media), observed through the Catalog in database.
 * See .scratch/manual-selection-unknown-media/specs.md.
 */
class UnknownManualIdentificationBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    val breakingBad = 1396L
    val inception = Artwork(id = 27205, title = "Inception", type = ContentType.MOVIE)
    val forgottenTale = Artwork(id = 990002, title = "Forgotten Tale", type = ContentType.MOVIE)
    val lostPages = Artwork(id = 990003, title = "Lost Pages", type = ContentType.SHOW)
    val lostPagesS01E01 = 70011L
    val lostPagesS01E02 = 70012L

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    test("identifying the last Unknown Movie attaches it to the chosen Movie, preserves Status and Progress, and deletes the Unknown Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Clip.mkv"))
        harness.lightSync()
        val unknown = harness.catalog.getUnknownMedias().single()
        harness.catalog.saveMedias(listOf(unknown.copy(status = Status.IS_WATCHING, currentTime = 1234L)))

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = forgottenTale,
        )

        // Then
        result.getOrNull() shouldBe forgottenTale.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe forgottenTale.id
        artwork.title shouldBe "Forgotten Tale"
        artwork.isManuallyIdentified shouldBe true
        val movie = harness.catalog.getMovie(artworkId = forgottenTale.id)!!
        movie.artworkId shouldBe forgottenTale.id
        movie.duration shouldBe 95
        movie.file.name shouldBe "Mystery Clip.mkv"
        movie.status shouldBe Status.IS_WATCHING
        movie.currentTime shouldBe 1234L
        harness.catalog.getUnknownMedias() shouldBe emptyList()
        harness.catalog.getArtwork(artworkId = Artwork.UNKNOWN_ID) shouldBe null

    }

    test("identifying an Unknown Movie attaches only the clicked file even when another Unknown Movie shares the same title") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Clip 1080p.mkv"),
            harness.deviceFile("Mystery Clip 720p.mkv"),
        )
        harness.lightSync()
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Clip 1080p.mkv" }

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = forgottenTale,
        )

        // Then
        result.getOrNull() shouldBe forgottenTale.id
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldContainExactlyInAnyOrder listOf(
            forgottenTale.id,
            Artwork.UNKNOWN_ID,
        )
        harness.catalog.getArtwork(artworkId = forgottenTale.id)?.isManuallyIdentified shouldBe true
        harness.catalog.getMovie(artworkId = forgottenTale.id)?.file?.name shouldBe "Mystery Clip 1080p.mkv"
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("Mystery Clip 720p.mkv")

    }

    test("identifying an Unknown Show episode groups all Unknown episodes of the same title and year, preserving Status and Progress") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Show S01E01.mkv"),
            harness.deviceFile("Mystery Show S01E02.mkv"),
            harness.deviceFile("Mystery Show S02E01.mkv"),
            harness.deviceFile("Mystery Show.mkv"),
            harness.deviceFile("Mystery Show (2024) S01E01.mkv"),
            harness.deviceFile("Other Show S01E01.mkv"),
        )
        harness.lightSync()
        val unknownByName = harness.catalog.getUnknownMedias().associateBy { it.file.name }
        harness.catalog.saveMedias(
            listOf(
                unknownByName.getValue("Mystery Show S01E01.mkv").copy(status = Status.IS_WATCHING, currentTime = 1234L),
                unknownByName.getValue("Mystery Show S02E01.mkv").copy(status = Status.WATCHED, currentTime = 2500L),
            )
        )
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        val show = harness.catalog.getArtwork(artworkId = lostPages.id)!!
        show.isManuallyIdentified shouldBe true
        harness.catalog.getSeasons(artworkId = lostPages.id).map { it.season } shouldContainExactlyInAnyOrder listOf(1, 2)
        val movedEpisodes = harness.catalog.getEpisodes(artworkId = lostPages.id)
        movedEpisodes.map { it.file.name to it.title } shouldContainExactlyInAnyOrder listOf(
            "Mystery Show S01E01.mkv" to "The Beginning",
            "Mystery Show S01E02.mkv" to "The Missing Page",
            "Mystery Show S02E01.mkv" to "The Next Chapter",
        )
        val movedS01E01 = movedEpisodes.single { it.file.name == "Mystery Show S01E01.mkv" }
        movedS01E01.status shouldBe Status.IS_WATCHING
        movedS01E01.currentTime shouldBe 1234L
        val movedS02E01 = movedEpisodes.single { it.file.name == "Mystery Show S02E01.mkv" }
        movedS02E01.status shouldBe Status.WATCHED
        movedS02E01.currentTime shouldBe 2500L
        harness.catalog.getUnknownMedias().map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Mystery Show.mkv",
            "Mystery Show (2024) S01E01.mkv",
            "Other Show S01E01.mkv",
        )

    }

    test("a blank-title Unknown episode only identifies itself and does not group unrelated blank-title episodes") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("S01E01.mkv"),
            harness.deviceFile("S01E02.mkv"),
        )
        harness.lightSync()
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "S01E01.mkv" }
        clicked.file.nameProperties.title shouldBe ""

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        harness.catalog.getArtwork(artworkId = lostPages.id)?.isManuallyIdentified shouldBe true
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name to it.id } shouldBe listOf(
            "S01E01.mkv" to lostPagesS01E01,
        )
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("S01E02.mkv")

    }

    test("an unlisted Unknown episode in a Season the chosen Show has is attached to the chosen Show") {

        // Given: Lost Pages Season 1 lists only E01 and E02
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Show S01E01.mkv"),
            harness.deviceFile("Mystery Show S01E03.mkv"),
        )
        harness.lightSync()
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Mystery Show S01E01.mkv",
            "Mystery Show S01E03.mkv",
        )
        harness.catalog.getUnknownMedias() shouldBe emptyList()
        harness.catalog.getArtwork(artworkId = Artwork.UNKNOWN_ID) shouldBe null

    }

    test("an Unknown episode of a Season the chosen Show does not have stays in the Unknown Artwork") {

        // Given: Lost Pages has Seasons 1 and 2, but no Season 3
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Show S01E01.mkv"),
            harness.deviceFile("Mystery Show S03E01.mkv"),
        )
        harness.lightSync()
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldContainExactlyInAnyOrder listOf(
            lostPages.id,
            Artwork.UNKNOWN_ID,
        )
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Mystery Show S01E01.mkv")
        harness.catalog.getSeasons(artworkId = lostPages.id).map { it.season } shouldBe listOf(1)
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("Mystery Show S03E01.mkv")

    }

    test("the Unknown episodes of a Season that fails stay in the Unknown Artwork with their Status and Progress while other Seasons move") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Show S01E01.mkv"),
            harness.deviceFile("Mystery Show S02E01.mkv"),
        )
        harness.lightSync()
        val unknownByName = harness.catalog.getUnknownMedias().associateBy { it.file.name }
        harness.catalog.saveMedias(
            listOf(
                unknownByName.getValue("Mystery Show S01E01.mkv").copy(status = Status.IS_WATCHING, currentTime = 1111L),
                unknownByName.getValue("Mystery Show S02E01.mkv").copy(status = Status.WATCHED, currentTime = 2222L),
            )
        )
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }
        harness.tmdb.failEndpoint("tv/${lostPages.id}/season/2")

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        val moved = harness.catalog.getEpisodes(artworkId = lostPages.id).single()
        moved.file.name shouldBe "Mystery Show S01E01.mkv"
        moved.status shouldBe Status.IS_WATCHING
        moved.currentTime shouldBe 1111L
        val stayed = harness.catalog.getUnknownMedias().single()
        stayed.file.name shouldBe "Mystery Show S02E01.mkv"
        stayed.status shouldBe Status.WATCHED
        stayed.currentTime shouldBe 2222L

    }

    test("when no Unknown episode matches the chosen Show, nothing changes") {

        // Given: Lost Pages has Season 1 without E03, and no Season 3
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Show S01E03.mkv"),
            harness.deviceFile("Mystery Show S03E01.mkv"),
        )
        harness.lightSync()
        val s01e03 = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E03.mkv" }
        harness.catalog.saveMedias(listOf(s01e03.copy(status = Status.IS_WATCHING, currentTime = 3333L)))
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E03.mkv" }

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(Artwork.UNKNOWN_ID)
        harness.catalog.getUnknownMedias().map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Mystery Show S01E03.mkv",
            "Mystery Show S03E01.mkv",
        )
        val unchanged = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E03.mkv" }
        unchanged.status shouldBe Status.IS_WATCHING
        unchanged.currentTime shouldBe 3333L

    }

    test("when the chosen Movie cannot be fetched for an Unknown Movie, nothing changes") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Clip.mkv"))
        harness.lightSync()
        val unknown = harness.catalog.getUnknownMedias().single()
        harness.catalog.saveMedias(listOf(unknown.copy(status = Status.WATCHED, currentTime = 999L)))
        harness.tmdb.cutNetwork()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = forgottenTale,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(Artwork.UNKNOWN_ID)
        val unchanged = harness.catalog.getUnknownMedias().single()
        unchanged.file.name shouldBe "Mystery Clip.mkv"
        unchanged.status shouldBe Status.WATCHED
        unchanged.currentTime shouldBe 999L

    }

    test("when the chosen Show cannot be fetched for an Unknown Show, nothing changes") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Mystery Show S01E01.mkv"),
            harness.deviceFile("Mystery Show S02E01.mkv"),
        )
        harness.lightSync()
        val s01e01 = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }
        harness.catalog.saveMedias(listOf(s01e01.copy(status = Status.WATCHED, currentTime = 999L)))
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }
        harness.tmdb.cutNetwork()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(Artwork.UNKNOWN_ID)
        harness.catalog.getUnknownMedias().map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Mystery Show S01E01.mkv",
            "Mystery Show S02E01.mkv",
        )
        val unchanged = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E01.mkv" }
        unchanged.status shouldBe Status.WATCHED
        unchanged.currentTime shouldBe 999L

    }

    test("merging an Unknown Movie into an existing public Movie preserves its History and Custom image") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forgotten Tale (2015).mkv"),
            harness.deviceFile("Mystery Clip.mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkCustomBackdrop(artworkId = forgottenTale.id, path = "/tale-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = forgottenTale.id, path = "/tale-poster.jpg")
        harness.catalog.setArtworkCustomLogo(artworkId = forgottenTale.id, path = "/tale-logo.png")
        harness.history.upsert(HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 2000L))
        val unknown = harness.catalog.getUnknownMedias().single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = forgottenTale,
        )

        // Then
        result.getOrNull() shouldBe forgottenTale.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe forgottenTale.id
        artwork.isPrivate shouldBe false
        artwork.isManuallyIdentified shouldBe true
        artwork.customBackdropPath shouldBe "/tale-backdrop.jpg"
        artwork.customPosterPath shouldBe "/tale-poster.jpg"
        artwork.customLogoPath shouldBe "/tale-logo.png"
        harness.catalog.getMovie(artworkId = forgottenTale.id)?.file?.name shouldBe "Mystery Clip.mkv"
        harness.catalog.getUnknownMedias() shouldBe emptyList()
        harness.history.getAll() shouldBe listOf(
            HistoryEntity(artworkId = forgottenTale.id, mediaId = forgottenTale.id, timestamp = 2000L),
        )

    }

    test("merging an Unknown Movie into an existing Private Movie preserves isPrivate and Custom image") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forgotten Tale (2015).mkv"),
            harness.deviceFile("Mystery Clip.mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkPrivate(artworkId = forgottenTale.id, isPrivate = true)
        harness.catalog.setArtworkCustomBackdrop(artworkId = forgottenTale.id, path = "/tale-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = forgottenTale.id, path = "/tale-poster.jpg")
        val unknown = harness.catalog.getUnknownMedias().single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = forgottenTale,
        )

        // Then
        result.getOrNull() shouldBe forgottenTale.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe forgottenTale.id
        artwork.isPrivate shouldBe true
        artwork.isManuallyIdentified shouldBe true
        artwork.customBackdropPath shouldBe "/tale-backdrop.jpg"
        artwork.customPosterPath shouldBe "/tale-poster.jpg"
        harness.catalog.getMovie(artworkId = forgottenTale.id)?.file?.name shouldBe "Mystery Clip.mkv"
        harness.catalog.getUnknownMedias() shouldBe emptyList()
        harness.history.getAll() shouldBe emptyList()

    }

    test("merging an Unknown Show into an existing public Show merges episodes and preserves its History and Custom image") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Lost Pages S01E01.mkv"),
            harness.deviceFile("Mystery Show S01E02.mkv"),
            harness.deviceFile("Mystery Show S02E01.mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkCustomBackdrop(artworkId = lostPages.id, path = "/pages-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = lostPages.id, path = "/pages-poster.jpg")
        harness.catalog.setArtworkCustomLogo(artworkId = lostPages.id, path = "/pages-logo.png")
        harness.history.upsert(HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS01E01, timestamp = 2000L))
        val clicked = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E02.mkv" }

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe lostPages.id
        artwork.isPrivate shouldBe false
        artwork.isManuallyIdentified shouldBe true
        artwork.customBackdropPath shouldBe "/pages-backdrop.jpg"
        artwork.customPosterPath shouldBe "/pages-poster.jpg"
        artwork.customLogoPath shouldBe "/pages-logo.png"
        harness.catalog.getSeasons(artworkId = lostPages.id).map { it.season } shouldContainExactlyInAnyOrder listOf(1, 2)
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Lost Pages S01E01.mkv",
            "Mystery Show S01E02.mkv",
            "Mystery Show S02E01.mkv",
        )
        harness.catalog.getUnknownMedias() shouldBe emptyList()
        harness.history.getAll() shouldBe listOf(
            HistoryEntity(artworkId = lostPages.id, mediaId = lostPagesS01E01, timestamp = 2000L),
        )

    }

    test("merging an Unknown Show into an existing Private Show merges episodes and preserves isPrivate and Custom image") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Lost Pages S01E01.mkv"),
            harness.deviceFile("Mystery Show S02E01.mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkPrivate(artworkId = lostPages.id, isPrivate = true)
        harness.catalog.setArtworkCustomBackdrop(artworkId = lostPages.id, path = "/pages-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = lostPages.id, path = "/pages-poster.jpg")
        val clicked = harness.catalog.getUnknownMedias().single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = clicked),
            target = lostPages,
        )

        // Then
        result.getOrNull() shouldBe lostPages.id
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe lostPages.id
        artwork.isPrivate shouldBe true
        artwork.isManuallyIdentified shouldBe true
        artwork.customBackdropPath shouldBe "/pages-backdrop.jpg"
        artwork.customPosterPath shouldBe "/pages-poster.jpg"
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldContainExactlyInAnyOrder listOf(
            "Lost Pages S01E01.mkv",
            "Mystery Show S02E01.mkv",
        )
        harness.catalog.getUnknownMedias() shouldBe emptyList()
        harness.history.getAll() shouldBe emptyList()

    }

    test("a Light sync attaches a new episode of the same title and year to the manually identified Show without a name search") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Show S01E01.mkv"))
        harness.lightSync()
        val unknown = harness.catalog.getUnknownMedias().single()
        harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = lostPages,
        ).getOrThrow()
        harness.tmdb.resetCalls()

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Mystery Show S01E02.mkv")
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/tv") shouldBe 0
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.id shouldBe lostPages.id
        artwork.isManuallyIdentified shouldBe true
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name to it.id } shouldContainExactlyInAnyOrder listOf(
            "Mystery Show S01E01.mkv" to lostPagesS01E01,
            "Mystery Show S01E02.mkv" to lostPagesS01E02,
        )
        harness.catalog.getUnknownMedias() shouldBe emptyList()

    }

    test("a Full sync resets isManuallyIdentified and returns unrecognized files to Unknown with Status and Progress preserved") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Lost Pages S01E01.mkv"),
            harness.deviceFile("Mystery Show S01E02.mkv"),
            harness.deviceFile("Mystery Clip.mkv"),
        )
        harness.lightSync()
        val unknownEpisode = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Show S01E02.mkv" }
        val unknownMovie = harness.catalog.getUnknownMedias().single { it.file.name == "Mystery Clip.mkv" }
        harness.identifyManually(source = IdentificationSource.UnknownMedia(media = unknownEpisode), target = lostPages).getOrThrow()
        harness.identifyManually(source = IdentificationSource.UnknownMedia(media = unknownMovie), target = forgottenTale).getOrThrow()

        val movedEpisode = harness.catalog.getEpisodes(artworkId = lostPages.id).single { it.file.name == "Mystery Show S01E02.mkv" }
        val movedMovie = harness.catalog.getMovie(artworkId = forgottenTale.id)!!
        harness.catalog.saveMedias(
            listOf(
                movedEpisode.copy(status = Status.IS_WATCHING, currentTime = 1234L),
                movedMovie.copy(status = Status.WATCHED, currentTime = 5678L),
            )
        )

        // When
        harness.fullSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldContainExactlyInAnyOrder listOf(
            lostPages.id,
            Artwork.UNKNOWN_ID,
        )
        harness.catalog.getArtwork(artworkId = lostPages.id)?.isManuallyIdentified shouldBe false
        harness.catalog.getArtwork(artworkId = forgottenTale.id) shouldBe null
        harness.catalog.getEpisodes(artworkId = lostPages.id).map { it.file.name } shouldBe listOf("Lost Pages S01E01.mkv")
        val unknownByName = harness.catalog.getUnknownMedias().associateBy { it.file.name }
        unknownByName.keys shouldContainExactlyInAnyOrder listOf("Mystery Show S01E02.mkv", "Mystery Clip.mkv")
        unknownByName.getValue("Mystery Show S01E02.mkv").status shouldBe Status.IS_WATCHING
        unknownByName.getValue("Mystery Show S01E02.mkv").currentTime shouldBe 1234L
        unknownByName.getValue("Mystery Clip.mkv").status shouldBe Status.WATCHED
        unknownByName.getValue("Mystery Clip.mkv").currentTime shouldBe 5678L

    }

    test("an Unknown Movie cannot be identified as a Show") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Clip.mkv"))
        harness.lightSync()
        val unknown = harness.catalog.getUnknownMedias().single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = lostPages,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(Artwork.UNKNOWN_ID)
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("Mystery Clip.mkv")

    }

    test("an Unknown Show episode cannot be identified as a Movie") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Show S01E01.mkv"))
        harness.lightSync()
        val unknown = harness.catalog.getUnknownMedias().single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = forgottenTale,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(Artwork.UNKNOWN_ID)
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("Mystery Show S01E01.mkv")

    }

    test("an Unknown Media cannot be identified as the Unknown Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Show S01E01.mkv"))
        harness.lightSync()
        val unknown = harness.catalog.getUnknownMedias().single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = unknown),
            target = Artwork.UNKNOWN,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(Artwork.UNKNOWN_ID)
        harness.catalog.getUnknownMedias().map { it.file.name } shouldBe listOf("Mystery Show S01E01.mkv")

    }

    test("identifying an Unknown Media that is no longer in Unknown fails without changing the Catalog") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Mystery Clip.mkv"))
        harness.lightSync()
        val stale = harness.catalog.getUnknownMedias().single()
        harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = stale),
            target = forgottenTale,
        ).getOrThrow()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = stale),
            target = inception,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(forgottenTale.id)
        harness.catalog.getMovie(artworkId = forgottenTale.id)?.file?.name shouldBe "Mystery Clip.mkv"

    }

    test("a known Episode cannot be identified as an UnknownMedia source") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.lightSync()
        val knownEpisode = harness.catalog.getEpisodes(artworkId = breakingBad).single()

        // When
        val result = harness.identifyManually(
            source = IdentificationSource.UnknownMedia(media = knownEpisode),
            target = lostPages,
        )

        // Then
        result.isFailure shouldBe true
        harness.catalog.getArtworks(includePrivates = true).map { it.id } shouldBe listOf(breakingBad)
        harness.catalog.getArtwork(artworkId = breakingBad)?.isManuallyIdentified shouldBe false

    }

})
