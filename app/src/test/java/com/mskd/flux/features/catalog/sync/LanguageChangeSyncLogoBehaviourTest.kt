package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.language.Language
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Behaviour of the Logos Sync step in refresh-all mode during a Language change,
 * observed through the Catalog in database, the Sync state, the images prefetch, and the TMDB calls.
 */
class LanguageChangeSyncLogoBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest {
        harness = SyncHarness()
        harness.settings.setDataLanguage(Language.ENGLISH)
    }
    afterTest { harness.close() }

    suspend fun logoOf(artworkId: Long): String? =
        harness.catalog.getArtwork(artworkId = artworkId)?.logoPath

    fun imagesCallCount() =
        harness.tmdb.callCount("movie/{id}/images") + harness.tmdb.callCount("tv/{id}/images")

    suspend fun englishCatalog(vararg files: String) {
        harness.deviceStorage.availableFiles = files.map { harness.deviceFile(it) }
        harness.lightSync()
        harness.tmdb.resetCalls()
    }

    test("during a Language change, every Artwork has its Logo searched again in the new data language without details calls") {

        // Given
        englishCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        logoOf(27205L) shouldBe "/inception-en.png"
        logoOf(1396L) shouldBe "/breaking-bad-textless.png"

        // When: switching to French
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then: Inception gets its French Logo
        logoOf(27205L) shouldBe "/inception-fr-best.png"
        logoOf(1396L) shouldBe "/breaking-bad-textless.png"
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.tmdb.callCount("tv/{id}") shouldBe 0
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

        // When: switching to German
        harness.settings.setDataLanguage(Language.GERMAN)
        harness.languageChangeSync()

        // Then: Breaking Bad gets its German Logo
        logoOf(1396L) shouldBe "/breaking-bad-de.png"
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.tmdb.callCount("tv/{id}") shouldBe 0
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("during a Light sync, only Artwork never searched before are searched for a Logo") {

        // Given
        englishCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        harness.catalog.setArtworkLogo(artworkId = 1396L, logoPath = null)
        harness.settings.setDataLanguage(Language.GERMAN)

        // When
        harness.lightSync()

        // Then: Inception keeps its English Logo without an images lookup; Breaking Bad is backfilled in German
        logoOf(27205L) shouldBe "/inception-en.png"
        logoOf(1396L) shouldBe "/breaking-bad-de.png"
        harness.tmdb.callCount("movie/{id}/images") shouldBe 0
        harness.tmdb.callCount("tv/{id}/images") shouldBe 1

    }

    test("a Private Artwork gets its new Logo during a Language change") {

        // Given
        englishCatalog("Inception (2010).mkv")
        harness.catalog.setArtworkPrivate(artworkId = 27205L, isPrivate = true)

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val inception = harness.catalog.getArtwork(artworkId = 27205L)!!
        inception.logoPath shouldBe "/inception-fr-best.png"
        inception.isPrivate shouldBe true

    }

    test("the Unknown Artwork is never searched for a Logo during a Language change") {

        // Given
        englishCatalog("Holidays in Brittany.mkv")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).single { it.isUnknown }.logoPath shouldBe null
        imagesCallCount() shouldBe 0

    }

    test("an Artwork with no Logo gets one in the new language, and loses it back in the former language") {

        // Given
        englishCatalog("Forgotten Tale (2015).mkv")
        logoOf(990002L) shouldBe ""

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        logoOf(990002L) shouldBe "/forgotten-tale-fr.png"

        // When
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.languageChangeSync()

        // Then
        logoOf(990002L) shouldBe ""

    }

    test("a failed Logo lookup keeps the previous Logo, does not stop other Artwork, and records no Failed file") {

        // Given
        englishCatalog("Inception (2010).mkv", "Forgotten Tale (2015).mkv")
        harness.tmdb.failEndpoint("movie/27205/images")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        logoOf(27205L) shouldBe "/inception-en.png"
        logoOf(990002L) shouldBe "/forgotten-tale-fr.png"
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a network failure keeps the previous Logos without recording any Failed file") {

        // Given
        englishCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        harness.tmdb.cutNetwork()

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        logoOf(27205L) shouldBe "/inception-en.png"
        logoOf(1396L) shouldBe "/breaking-bad-textless.png"
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("without a TMDB token, no Logo lookups are fired and previous Logos are preserved cleanly") {

        // Given
        englishCatalog("Inception (2010).mkv")
        harness.token.saveToken("")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        logoOf(27205L) shouldBe "/inception-en.png"
        imagesCallCount() shouldBe 0
        harness.tmdb.totalCallCount shouldBe 0
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("Custom images (Backdrop, Poster, and Logo) are preserved during a Language change and a custom Logo skips the Logo lookup") {

        // Given
        englishCatalog("Inception (2010).mkv", "Forgotten Tale (2015).mkv")
        harness.catalog.setArtworkCustomBackdrop(artworkId = 27205L, path = "/custom-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = 27205L, path = "/custom-poster.jpg")
        harness.catalog.setArtworkCustomLogo(artworkId = 27205L, path = "/custom-logo.png")
        harness.catalog.setArtworkCustomBackdrop(artworkId = 990002L, path = "/tale-backdrop.jpg")
        harness.catalog.setArtworkCustomPoster(artworkId = 990002L, path = "/tale-poster.jpg")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.languageChangeSync()

        // Then
        val inception = harness.catalog.getArtwork(artworkId = 27205L)!!
        harness.tmdb.callCount("movie/27205/images") shouldBe 0
        inception.logoPath shouldBe "/inception-en.png"
        inception.customBackdropPath shouldBe "/custom-backdrop.jpg"
        inception.customPosterPath shouldBe "/custom-poster.jpg"
        inception.customLogoPath shouldBe "/custom-logo.png"

        val forgottenTale = harness.catalog.getArtwork(artworkId = 990002L)!!
        forgottenTale.logoPath shouldBe "/forgotten-tale-fr.png"
        forgottenTale.customBackdropPath shouldBe "/tale-backdrop.jpg"
        forgottenTale.customPosterPath shouldBe "/tale-poster.jpg"

    }

    test("the new Logo is not overwritten by the translated Artwork saved during a Language change") {

        // Given
        englishCatalog("Breaking Bad S01E01.mkv")
        // The translation is slow to come: a Logo saved meanwhile would be overwritten by the translated Artwork
        harness.tmdb.pause("tv/{id}/translations")

        // When
        harness.settings.setDataLanguage(Language.GERMAN)
        harness.requestLanguageChangeSync()
        withTimeout(10.seconds) {
            while (harness.tmdb.callCount("tv/{id}/translations") == 0) delay(10)
        }
        // Leaves time to a Logo searched before the end of the translations to be saved; none is expected
        withTimeoutOrNull(500.milliseconds) {
            while (harness.tmdb.callCount("tv/{id}/images") == 0) delay(10)
        }

        // Then
        harness.tmdb.callCount("tv/{id}/images") shouldBe 0

        // When
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        val breakingBad = harness.catalog.getArtwork(artworkId = 1396L)!!
        breakingBad.description shouldBe "Walter White, ein Chemielehrer, beginnt Methamphetamin herzustellen."
        breakingBad.logoPath shouldBe "/breaking-bad-de.png"

    }

    test("the images are prefetched once the Logos are updated, so they are available offline") {

        // Given
        englishCatalog("Inception (2010).mkv")
        val prefetchesBefore = harness.prefetch.prefetchCount
        harness.tmdb.pause("movie/{id}/images")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.requestLanguageChangeSync()
        withTimeout(10.seconds) {
            while (harness.tmdb.callCount("movie/{id}/images") == 0) delay(10)
        }

        // Then
        harness.prefetch.prefetchCount shouldBe prefetchesBefore

        // When
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        logoOf(27205L) shouldBe "/inception-fr-best.png"
        harness.prefetch.prefetchCount shouldBe prefetchesBefore + 1

    }

    test("Sync progress declares and completes units for Logo lookups during a Language change") {

        // Given
        englishCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv", "Forgotten Tale (2015).mkv")
        val statesBefore = harness.states.size
        harness.tmdb.pause("tv/{id}/images")

        // When
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.requestLanguageChangeSync()
        withTimeout(10.seconds) {
            while (
                logoOf(27205L) != "/inception-fr-best.png" ||
                logoOf(990002L) != "/forgotten-tale-fr.png" ||
                harness.tmdb.callCount("tv/{id}/images") == 0
            ) {
                delay(10)
            }
        }
        val duringLogos = harness.states.last().shouldBeInstanceOf<SyncState.Syncing>().progress

        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        logoOf(1396L) shouldBe "/breaking-bad-textless.png"
        val progresses = harness.states.drop(statesBefore).filterIsInstance<SyncState.Syncing>().map { it.progress }
        progresses.zipWithNext().all { (previous, next) -> next >= previous } shouldBe true
        progresses.distinct().size shouldBeGreaterThan 1
        (duringLogos > 0f && duringLogos < 0.85f) shouldBe true
        (progresses.dropLast(1).max() > duringLogos) shouldBe true
        progresses.dropLast(1).all { it < 1f } shouldBe true
        progresses.last() shouldBe 1f
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

})
