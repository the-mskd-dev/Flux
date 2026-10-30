package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.language.Language
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Logo given by the Étape "Logos" to the Artwork already in the Catalogue, observed through the Catalogue in database,
 * the Synchro state and the TMDB calls. See .scratch/artwork-logos/specs.md.
 */
class SyncLogoBackfillBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest {
        harness = SyncHarness()
        harness.settings.setDataLanguage(Language.ENGLISH)
    }
    afterTest { harness.close() }

    suspend fun logoOf(title: String): String? =
        harness.catalog.getArtworks(includePrivates = true).single { it.title == title }.logoPath

    fun imagesCallCount() = harness.tmdb.callCount("movie/{id}/images") + harness.tmdb.callCount("tv/{id}/images")

    /** A Catalogue synchronised before the Logo existed: its Artwork have never been searched for a Logo. */
    suspend fun oldCatalog(vararg files: String) {
        harness.deviceStorage.availableFiles = files.map { harness.deviceFile(it) }
        harness.lightSync()
        harness.forgetLogos()
        harness.tmdb.resetCalls()
    }

    test("an old Artwork gets its Logo in a light Synchro, with one images call") {

        // Given
        oldCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe "/inception-en.png"
        logoOf("Breaking Bad") shouldBe "/breaking-bad-textless.png"
        harness.tmdb.callCount("movie/{id}/images") shouldBe 1
        harness.tmdb.callCount("tv/{id}/images") shouldBe 1
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.tmdb.callCount("tv/{id}") shouldBe 0

    }

    test("in a Synchro complète, an old Artwork gets its Logo from the details call, with no images call") {

        // Given
        oldCatalog("Inception (2010).mkv")

        // When
        harness.fullSync()

        // Then
        logoOf("Inception") shouldBe "/inception-en.png"
        imagesCallCount() shouldBe 0

    }

    test("in a Synchro complète, the progress accounts only for the Logos left to the Étape") {

        // Given
        oldCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        val statesBefore = harness.states.size
        harness.tmdb.pause("tv/{id}/season/{season}")

        // When
        harness.requestFullSync()
        withTimeout(10.seconds) {
            while (logoOf("Inception") == null) delay(10)
        }
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        val progresses = harness.states.drop(statesBefore).filterIsInstance<SyncState.Syncing>().map { it.progress }
        // Before Finalisation, the work declared is done: the progress reaches its end, it does not stall
        progresses.dropLast(1).max() shouldBe 0.99f

    }

    test("the Logo of an old Artwork is chosen in the data language") {

        // Given
        oldCatalog("Inception (2010).mkv")
        harness.settings.setDataLanguage(Language.FRENCH)

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe "/inception-fr-best.png"

    }

    test("an old Artwork with no usable Logo on TMDB (SVG only) is saved with no Logo and not searched again") {

        // Given
        oldCatalog("Forgotten Tale (2015).mkv")

        // When
        harness.lightSync()
        harness.lightSync()

        // Then
        logoOf("Forgotten Tale") shouldBe ""
        imagesCallCount() shouldBe 1

    }

    test("an Artwork whose Logo is already known is not searched again") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        harness.tmdb.resetCalls()

        // When
        harness.lightSync()

        // Then
        imagesCallCount() shouldBe 0

    }

    test("a failed Logo lookup is retried at the next Synchro, without any Fichier en échec") {

        // Given
        oldCatalog("Inception (2010).mkv")
        harness.tmdb.failEndpoint("movie/{id}/images")

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe null
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

        // When
        harness.tmdb.restoreNetwork()
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe "/inception-en.png"

    }

    test("a network failure leaves the Logo unsearched") {

        // Given
        oldCatalog("Inception (2010).mkv")
        harness.tmdb.cutNetwork()

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe null
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("without a TMDB token, no Logo is searched and the Artwork stay unsearched") {

        // Given
        oldCatalog("Inception (2010).mkv")
        harness.token.saveToken("")

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe null
        imagesCallCount() shouldBe 0
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a failed Logo lookup does not prevent the other Artwork from getting theirs") {

        // Given
        oldCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        harness.tmdb.failEndpoint("movie/{id}/images")

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe null
        logoOf("Breaking Bad") shouldBe "/breaking-bad-textless.png"

    }

    test("the Unknown Artwork is never searched for a Logo") {

        // Given
        oldCatalog("Holidays in Brittany.mkv")

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).single { it.isUnknown }.logoPath shouldBe null
        imagesCallCount() shouldBe 0

    }

    test("a Private Artwork gets its Logo like the others") {

        // Given
        oldCatalog("Inception (2010).mkv")
        harness.catalog.setArtworkPrivate(artworkId = 27205, isPrivate = true)

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe "/inception-en.png"
        harness.catalog.getArtwork(artworkId = 27205)?.isPrivate shouldBe true

    }

    test("each Logo is saved as soon as it is found, before the end of the Synchro") {

        // Given
        oldCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv")
        harness.tmdb.pause("tv/{id}/images")

        // When
        harness.requestLightSync()

        // Then
        withTimeout(10.seconds) {
            while (logoOf("Inception") == null) delay(10)
        }
        harness.states.last().shouldBeInstanceOf<SyncState.Syncing>()
        logoOf("Breaking Bad") shouldBe null

        harness.tmdb.resume()
        harness.awaitIdle()
        logoOf("Breaking Bad") shouldBe "/breaking-bad-textless.png"

    }

    test("the progress accounts for the Logos, never goes backwards and only reaches 100 % when the Synchro ends") {

        // Given
        oldCatalog("Inception (2010).mkv", "Breaking Bad S01E01.mkv", "Forgotten Tale (2015).mkv")
        val statesBefore = harness.states.size
        harness.tmdb.pause("tv/{id}/images")

        // When
        harness.requestLightSync()
        withTimeout(10.seconds) {
            while (logoOf("Inception") == null || logoOf("Forgotten Tale") == null) delay(10)
        }
        val duringLogos = (harness.states.last() as SyncState.Syncing).progress
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        val progresses = harness.states.drop(statesBefore).filterIsInstance<SyncState.Syncing>().map { it.progress }
        progresses.zipWithNext().all { (previous, next) -> next >= previous } shouldBe true
        progresses.distinct().size shouldBeGreaterThan 1
        progresses.dropLast(1).all { it < 1f } shouldBe true
        // Two Logos out of three are done: the Synchro is far from its end
        (duringLogos < 0.9f) shouldBe true
        harness.states.last().shouldBeInstanceOf<SyncState.Idle>()

    }

})
