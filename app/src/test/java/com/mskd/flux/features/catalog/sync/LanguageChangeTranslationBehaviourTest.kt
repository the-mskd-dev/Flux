package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.language.Language
import com.mskd.flux.core.network.tmdb.data.TMDB_MAX_REQUESTS
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.history.data.model.HistoryEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Behaviour of the Translation Sync step during a Language change, observed through the Catalog
 * in database, user state preservation, Sync progress, and TMDB calls.
 */
class LanguageChangeTranslationBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest {
        harness = SyncHarness()
        harness.settings.setDataLanguage(Language.ENGLISH)
    }
    afterTest { harness.close() }

    test("a Language change translates Movies, Shows, Seasons, and Episodes, switches back cleanly, and is idempotent") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Lost Pages S01E01.mkv"),
        )
        harness.lightSync()

        // When: switching to French
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then: Movie (both Movie media and Artwork), Show, Season, and Episode are translated
        val movie = harness.catalog.getMovie(artworkId = 990001L)!!
        movie.title shouldBe "Film Interdit"
        movie.description shouldBe "Un film pour adultes."

        val movieArtwork = harness.catalog.getArtwork(artworkId = 990001L)!!
        movieArtwork.title shouldBe "Film Interdit"
        movieArtwork.description shouldBe "Un film pour adultes."

        val lostPages = harness.catalog.getArtwork(artworkId = 990003L)!!
        lostPages.title shouldBe "Pages Perdues"
        lostPages.description shouldBe "Des pages que personne n'a traduites."

        val season1 = harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }
        season1.title shouldBe "Saison 1"
        season1.description shouldBe "La vie de Walter White, professeur de chimie, bascule soudainement."

        val episode1 = harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }
        episode1.title shouldBe "Chute libre"
        episode1.description shouldBe "Walter White apprend qu'il est atteint d'un cancer du poumon en phase terminale."

        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

        // When: repeating the same language
        harness.languageChangeSync()

        // Then: idempotent
        harness.catalog.getMovie(artworkId = 990001L)!!.title shouldBe "Film Interdit"
        harness.catalog.getArtwork(artworkId = 990003L)!!.title shouldBe "Pages Perdues"
        harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }.title shouldBe "Saison 1"
        harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }.title shouldBe "Chute libre"
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

        // When: switching back to English
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.languageChangeSync()

        // Then: English titles and descriptions are restored
        val restoredMovie = harness.catalog.getMovie(artworkId = 990001L)!!
        restoredMovie.title shouldBe "Forbidden Film"
        restoredMovie.description shouldBe "An adult movie."

        val restoredMovieArtwork = harness.catalog.getArtwork(artworkId = 990001L)!!
        restoredMovieArtwork.title shouldBe "Forbidden Film"
        restoredMovieArtwork.description shouldBe "An adult movie."

        val restoredShow = harness.catalog.getArtwork(artworkId = 990003L)!!
        restoredShow.title shouldBe "Lost Pages"
        restoredShow.description shouldBe "Pages nobody translated."

        val restoredSeason = harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }
        restoredSeason.title shouldBe "Season 1"
        restoredSeason.description shouldBe "High school chemistry teacher Walter White's life is suddenly transformed."

        val restoredEpisode = harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }
        restoredEpisode.title shouldBe "Pilot"
        restoredEpisode.description shouldBe "Walter White learns that he has terminal lung cancer."

        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a missing translation keeps the previous title and description") {

        // Given: Forgotten Tale has neither French nor English entries in /translations
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forgotten Tale (2015).mkv"))
        harness.lightSync()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val movie = harness.catalog.getMovie(artworkId = 990002L)!!
        movie.title shouldBe "Forgotten Tale"
        movie.description shouldBe "A tale nobody translated."

        val artwork = harness.catalog.getArtwork(artworkId = 990002L)!!
        artwork.title shouldBe "Forgotten Tale"
        artwork.description shouldBe "A tale nobody translated."

    }

    test("a blank translated description keeps the previous description while updating the title") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forgotten Tale (2015).mkv"))
        harness.lightSync()

        // When: Spanish has title "Cuento Olvidado" and blank overview
        harness.settings.setDataLanguage(Language.SPANISH)
        harness.languageChangeSync()

        // Then
        val movie = harness.catalog.getMovie(artworkId = 990002L)!!
        movie.title shouldBe "Cuento Olvidado"
        movie.description shouldBe "A tale nobody translated."

        val artwork = harness.catalog.getArtwork(artworkId = 990002L)!!
        artwork.title shouldBe "Cuento Olvidado"
        artwork.description shouldBe "A tale nobody translated."

    }

    test("a blank translated title keeps the previous title while updating the description") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forgotten Tale (2015).mkv"))
        harness.lightSync()

        // When: Italian has blank title and overview "Una storia dimenticata."
        harness.settings.setDataLanguage(Language.ITALIAN)
        harness.languageChangeSync()

        // Then
        val movie = harness.catalog.getMovie(artworkId = 990002L)!!
        movie.title shouldBe "Forgotten Tale"
        movie.description shouldBe "Una storia dimenticata."

        val artwork = harness.catalog.getArtwork(artworkId = 990002L)!!
        artwork.title shouldBe "Forgotten Tale"
        artwork.description shouldBe "Una storia dimenticata."

    }

    test("a missing translated description field keeps the previous description while updating the title") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forgotten Tale (2015).mkv"))
        harness.lightSync()

        // When: German has title "Vergessene Geschichte" and omitted overview
        harness.settings.setDataLanguage(Language.GERMAN)
        harness.languageChangeSync()

        // Then
        val movie = harness.catalog.getMovie(artworkId = 990002L)!!
        movie.title shouldBe "Vergessene Geschichte"
        movie.description shouldBe "A tale nobody translated."

        val artwork = harness.catalog.getArtwork(artworkId = 990002L)!!
        artwork.title shouldBe "Vergessene Geschichte"
        artwork.description shouldBe "A tale nobody translated."

    }

    test("a missing translated title field keeps the previous title while updating the description") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forgotten Tale (2015).mkv"))
        harness.lightSync()

        // When: Portuguese has omitted title and overview "Um conto esquecido."
        harness.settings.setDataLanguage(Language.PORTUGUESE)
        harness.languageChangeSync()

        // Then
        val movie = harness.catalog.getMovie(artworkId = 990002L)!!
        movie.title shouldBe "Forgotten Tale"
        movie.description shouldBe "Um conto esquecido."

        val artwork = harness.catalog.getArtwork(artworkId = 990002L)!!
        artwork.title shouldBe "Forgotten Tale"
        artwork.description shouldBe "Um conto esquecido."

    }

    test("a failed translation lookup leaves that item untouched without stopping the others or recording a Failed file") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        harness.tmdb.failEndpoint("movie/990001/translations")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then: Forbidden Film keeps English title & description
        val forbiddenMovie = harness.catalog.getMovie(artworkId = 990001L)!!
        forbiddenMovie.title shouldBe "Forbidden Film"
        forbiddenMovie.description shouldBe "An adult movie."
        val forbiddenArtwork = harness.catalog.getArtwork(artworkId = 990001L)!!
        forbiddenArtwork.title shouldBe "Forbidden Film"
        forbiddenArtwork.description shouldBe "An adult movie."

        // And Breaking Bad Show, Season 1, and Episode 1 are translated
        harness.catalog.getArtwork(artworkId = 1396L)!!.description shouldBe
            "Walter White, professeur de chimie, se met à fabriquer de la méthamphétamine."
        harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }.title shouldBe "Saison 1"
        harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }.title shouldBe "Chute libre"
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("without network, a Language change ends without crashing, keeps existing texts, and records no Failed file") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        harness.tmdb.cutNetwork()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val movie = harness.catalog.getMovie(artworkId = 990001L)!!
        movie.title shouldBe "Forbidden Film"
        movie.description shouldBe "An adult movie."
        harness.catalog.getArtwork(artworkId = 1396L)!!.description shouldBe
            "Walter White, a chemistry teacher, turns to manufacturing methamphetamine."
        harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }.title shouldBe "Season 1"
        harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }.title shouldBe "Pilot"
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("without a TMDB token on a populated Catalog, no translation lookups are fired and the Sync completes cleanly") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        harness.token.saveToken("")
        harness.tmdb.resetCalls()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        harness.tmdb.totalCallCount shouldBe 0
        harness.catalog.getMovie(artworkId = 990001L)!!.title shouldBe "Forbidden Film"
        harness.catalog.getArtwork(artworkId = 1396L)!!.description shouldBe
            "Walter White, a chemistry teacher, turns to manufacturing methamphetamine."
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("Private Artwork and Unavailable Media from an Unavailable source are translated") {

        // Given
        val usbRoot = "content://com.android.externalstorage/tree/usb"
        val usbSource = harness.addCustomSource(
            root = usbRoot,
            files = listOf(harness.customFile(root = usbRoot, name = "Breaking Bad S01E01.mkv")),
            available = true,
        )
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forbidden Film (2020).mkv"))
        harness.lightSync()

        harness.catalog.setArtworkPrivate(artworkId = 990001L, isPrivate = true)
        usbSource.available = false

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then: Private Artwork is translated and remains private
        val privateMovie = harness.catalog.getMovie(artworkId = 990001L)!!
        privateMovie.title shouldBe "Film Interdit"
        privateMovie.description shouldBe "Un film pour adultes."
        val privateArtwork = harness.catalog.getArtwork(artworkId = 990001L)!!
        privateArtwork.title shouldBe "Film Interdit"
        privateArtwork.isPrivate shouldBe true

        // And Unavailable Media (Show, Season, Episode) is translated and kept in Catalog
        harness.catalog.getArtwork(artworkId = 1396L)!!.description shouldBe
            "Walter White, professeur de chimie, se met à fabriquer de la méthamphétamine."
        harness.catalog.getSeasons(artworkId = 1396L).single { it.season == 1 }.title shouldBe "Saison 1"
        harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }.title shouldBe "Chute libre"

    }

    test("the Unknown Artwork is left untouched and triggers no translation lookups") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Holidays in Brittany.mkv"),
        )
        harness.lightSync()
        harness.tmdb.resetCalls()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val unknownArtwork = harness.catalog.getArtworks(includePrivates = true).single { it.isUnknown }
        unknownArtwork.title shouldBe "Others"
        unknownArtwork.description shouldBe ""

        val unknownEpisode = harness.catalog.getUnknownMedias().single()
        unknownEpisode.title shouldBe "holidays in brittany"
        unknownEpisode.description shouldBe ""

        harness.tmdb.callCount("tv/{id}/translations") shouldBe 0
        harness.tmdb.callCount("tv/{id}/season/{season}/translations") shouldBe 0
        harness.tmdb.callCount("tv/{id}/season/{season}/episode/{number}/translations") shouldBe 0
        harness.catalog.getMovie(artworkId = 990001L)!!.title shouldBe "Film Interdit"

    }

    test("Progress, Status, History, Custom images, and Manual identification are preserved during translation") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Forgotten Tale (2015).mkv"),
        )
        harness.lightSync()

        val movie = harness.catalog.getMovie(artworkId = 990001L)!!
        val episode = harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }
        harness.catalog.saveMedias(
            listOf(
                movie.copy(currentTime = 42_000L, status = Status.IS_WATCHING),
                episode.copy(currentTime = 17_000L, status = Status.IS_WATCHING),
            )
        )
        harness.history.upsert(HistoryEntity(artworkId = 990001L, mediaId = movie.mediaId, timestamp = 1_000L))
        harness.history.upsert(HistoryEntity(artworkId = 1396L, mediaId = episode.mediaId, timestamp = 2_000L))

        harness.catalog.setArtworkCustomPoster(artworkId = 990001L, path = "/custom-poster.jpg")
        harness.catalog.setArtworkCustomBackdrop(artworkId = 990001L, path = "/custom-backdrop.jpg")
        harness.catalog.setArtworkCustomLogo(artworkId = 990001L, path = "/custom-logo.png")

        harness.identifyManually(
            artworkId = 990002L,
            target = Artwork(id = 27205L, title = "Inception", type = ContentType.MOVIE),
        ).getOrThrow()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then: translations applied
        val translatedMovie = harness.catalog.getMovie(artworkId = 990001L)!!
        translatedMovie.title shouldBe "Film Interdit"
        translatedMovie.currentTime shouldBe 42_000L
        translatedMovie.status shouldBe Status.IS_WATCHING

        val translatedEpisode = harness.catalog.getEpisodes(artworkId = 1396L).single { it.season == 1 && it.number == 1 }
        translatedEpisode.title shouldBe "Chute libre"
        translatedEpisode.currentTime shouldBe 17_000L
        translatedEpisode.status shouldBe Status.IS_WATCHING

        harness.history.get(artworkId = 990001L) shouldBe HistoryEntity(artworkId = 990001L, mediaId = movie.mediaId, timestamp = 1_000L)
        harness.history.get(artworkId = 1396L) shouldBe HistoryEntity(artworkId = 1396L, mediaId = episode.mediaId, timestamp = 2_000L)

        val forbiddenArtwork = harness.catalog.getArtwork(artworkId = 990001L)!!
        forbiddenArtwork.title shouldBe "Film Interdit"
        forbiddenArtwork.customPosterPath shouldBe "/custom-poster.jpg"
        forbiddenArtwork.customBackdropPath shouldBe "/custom-backdrop.jpg"
        forbiddenArtwork.customLogoPath shouldBe "/custom-logo.png"

        val inceptionArtwork = harness.catalog.getArtwork(artworkId = 27205L)!!
        inceptionArtwork.isManuallyIdentified shouldBe true
        inceptionArtwork.description shouldBe "Dom Cobb est un voleur expérimenté dans l'art périlleux de l'extraction."

    }

    test("Sync progress advances monotonically during translation and reaches 100% only at completion") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forbidden Film (2020).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        val statesBefore = harness.states.size

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val progresses = harness.states
            .drop(statesBefore)
            .filterIsInstance<SyncState.Syncing>()
            .map { it.progress }
        progresses.zipWithNext().all { (previous, next) -> next >= previous } shouldBe true
        progresses.filter { it < 1f }.distinct().size shouldBeGreaterThan 2
        progresses.dropLast(1).all { it < 1f } shouldBe true
        progresses.last() shouldBe 1f
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("Translation runs before the Logos step so translated Artwork rows never overwrite new Logo paths") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.lightSync()
        harness.forgetLogos()
        harness.tmdb.resetCalls()
        harness.tmdb.pause("tv/{id}/translations")

        // When
        harness.settings.setDataLanguage(Language.GERMAN)
        harness.requestLanguageChangeSync()
        withTimeout(10.seconds) {
            while (harness.tmdb.callCount("tv/{id}/translations") == 0) delay(10)
        }
        withTimeoutOrNull(500.milliseconds) {
            while (harness.tmdb.callCount("tv/{id}/images") == 0) delay(10)
        }

        // Then: Logos step has not started while translations are still paused
        harness.tmdb.callCount("tv/{id}/images") shouldBe 0

        // When
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then: both German translation and German Logo are persisted
        val breakingBad = harness.catalog.getArtwork(artworkId = 1396L)!!
        breakingBad.description shouldBe "Walter White, ein Chemielehrer, beginnt Methamphetamin herzustellen."
        breakingBad.logoPath shouldBe "/breaking-bad-de.png"

    }

    test("a Catalog larger than one batch translates all items within the TMDB call limit") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forbidden Film (2020).mkv"))
        harness.lightSync()

        val seededIds = (1..25).map { 800_000L + it }
        harness.catalog.saveArtworks(
            seededIds.map { id -> Artwork(id = id, title = "Seeded $id", type = ContentType.MOVIE, logoPath = "") }
        )
        harness.catalog.saveMedias(
            seededIds.map { id ->
                Movie(
                    artworkId = id,
                    title = "Seeded $id",
                    releaseDateString = "",
                    description = "Desc $id",
                    voteAverage = 0f,
                    voteCount = 0,
                    duration = 90,
                    file = harness.deviceFile("Seeded $id (2020).mkv"),
                    isAvailable = true,
                )
            }
        )
        harness.tmdb.latencyMillis = 5
        harness.tmdb.resetCalls()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        harness.catalog.getMovie(artworkId = 990001L)!!.title shouldBe "Film Interdit"
        harness.tmdb.callCount("movie/{id}/translations") shouldBe 26
        harness.tmdb.maxInFlight shouldBeLessThanOrEqual TMDB_MAX_REQUESTS
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("Cast and Crew of translated Movies and Shows are preserved") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        harness.catalog.getArtwork(artworkId = 27205L)!!.description shouldBe
            "Dom Cobb est un voleur expérimenté dans l'art périlleux de l'extraction."
        harness.catalog.getArtwork(artworkId = 1396L)!!.description shouldBe
            "Walter White, professeur de chimie, se met à fabriquer de la méthamphétamine."
        harness.people.flowPeople(artworkId = 27205L).first().map { it.name } shouldContainExactlyInAnyOrder listOf(
            "Leonardo DiCaprio",
            "Christopher Nolan",
        )
        harness.people.flowPeople(artworkId = 1396L).first().map { it.name } shouldContainExactlyInAnyOrder listOf(
            "Bryan Cranston",
        )

    }

})
