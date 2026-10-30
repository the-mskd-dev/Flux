package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.language.Language
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Logo chosen again when the data language changes, observed through the Catalogue in database,
 * the TMDB calls and the images prefetch. See .scratch/artwork-logos/specs.md.
 */
class LanguageChangeLogoBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest {
        harness = SyncHarness()
        harness.settings.setDataLanguage(Language.ENGLISH)
    }
    afterTest { harness.close() }

    suspend fun logoOf(title: String): String? =
        harness.catalog.getArtworks(includePrivates = true).single { it.title == title }.logoPath

    /** A Catalogue synchronised in English, with the Logos chosen in English. */
    suspend fun englishCatalog(vararg files: String) {
        harness.deviceStorage.availableFiles = files.map { harness.deviceFile(it) }
        harness.lightSync()
        harness.tmdb.resetCalls()
    }

    test("the Logo of every Artwork is chosen again in the new language, without any Synchro") {

        // Given
        englishCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        logoOf("Inception") shouldBe "/inception-en.png"

        // When
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        logoOf("Inception") shouldBe "/inception-fr-best.png"
        logoOf("Breaking Bad") shouldBe "/breaking-bad-textless.png"
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.tmdb.callCount("tv/{id}") shouldBe 0

    }

    test("an Artwork with no Logo gets one in the new language, and loses it back in the former language") {

        // Given
        englishCatalog("Forgotten Tale (2015).mkv")
        logoOf("Forgotten Tale") shouldBe ""

        // When
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        logoOf("Forgotten Tale") shouldBe "/forgotten-tale-fr.png"

        // When
        harness.changeDataLanguage(Language.ENGLISH)

        // Then
        logoOf("Forgotten Tale") shouldBe ""

    }

    test("a failed Logo lookup keeps the previous Logo, without preventing the other Artwork from getting theirs") {

        // Given
        englishCatalog("Inception (2010).mkv", "Forgotten Tale (2015).mkv")
        harness.tmdb.failEndpoint("movie/27205/images")

        // When
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        logoOf("Inception") shouldBe "/inception-en.png"
        logoOf("Forgotten Tale") shouldBe "/forgotten-tale-fr.png"
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("the new Logo is not overwritten by the translated Artwork saved by the language change") {

        // Given
        englishCatalog("Breaking Bad S01E01.mkv")
        // The translation is slow to come: a Logo saved meanwhile would be overwritten by the translated Artwork
        harness.tmdb.pause("tv/{id}/translations")

        // When
        harness.requestDataLanguageChange(Language.GERMAN)
        // Leaves time to a Logo searched before the end of the translations to be saved; none is expected
        withTimeoutOrNull(500.milliseconds) {
            while (harness.tmdb.callCount("tv/{id}/images") == 0) delay(10)
        }
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        val breakingBad = harness.catalog.getArtwork(artworkId = 1396)!!
        breakingBad.description shouldBe "Walter White, ein Chemielehrer, beginnt Methamphetamin herzustellen."
        breakingBad.logoPath shouldBe "/breaking-bad-de.png"

    }

    test("the images are prefetched once the Logos are updated, so they are available offline") {

        // Given
        englishCatalog("Inception (2010).mkv")
        val prefetchesBefore = harness.prefetch.prefetchCount
        harness.tmdb.pause("movie/{id}/images")

        // When
        harness.requestDataLanguageChange(Language.FRENCH)
        withTimeout(10.seconds) {
            while (harness.tmdb.callCount("movie/{id}/images") == 0) delay(10)
        }

        // Then
        harness.prefetch.prefetchCount shouldBe prefetchesBefore

        // When
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.prefetch.prefetchCount shouldBe prefetchesBefore + 1

    }

    test("the Unknown Artwork is never searched for a Logo") {

        // Given
        englishCatalog("Holidays in Brittany.mkv")

        // When
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        harness.catalog.getArtworks(includePrivates = true).single { it.isUnknown }.logoPath shouldBe null
        harness.tmdb.callCount("movie/{id}/images") + harness.tmdb.callCount("tv/{id}/images") shouldBe 0

    }

    test("a Private Artwork gets its new Logo like the others") {

        // Given
        englishCatalog("Inception (2010).mkv")
        harness.catalog.setArtworkPrivate(artworkId = 27205, isPrivate = true)

        // When
        harness.changeDataLanguage(Language.FRENCH)

        // Then
        logoOf("Inception") shouldBe "/inception-fr-best.png"
        harness.catalog.getArtwork(artworkId = 27205)?.isPrivate shouldBe true

    }

})
