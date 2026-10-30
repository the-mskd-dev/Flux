package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.language.Language
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Logo of the Artwork recorded by the Synchro, observed through the Catalogue in database and the TMDB calls.
 * See .scratch/artwork-logos/specs.md.
 */
class SyncLogoBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    suspend fun logoOf(title: String): String? =
        harness.catalog.getArtworks(includePrivates = true).single { it.title == title }.logoPath

    test("a new Artwork gets its Logo from the details call, with no images call") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe "/inception-en.png"
        harness.tmdb.callCount("movie/{id}/images") shouldBe 0
        harness.tmdb.callCount("tv/{id}/images") shouldBe 0

    }

    test("the Logo in the data language is preferred, the best rated PNG of that language") {

        // Given
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))

        // When
        harness.lightSync()

        // Then
        logoOf("Inception") shouldBe "/inception-fr-best.png"

    }

    test("without a Logo in the data language nor in English, the language-less Logo is chosen") {

        // Given
        harness.settings.setDataLanguage(Language.FRENCH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))

        // When
        harness.lightSync()

        // Then
        logoOf("Breaking Bad") shouldBe "/breaking-bad-textless.png"

    }

    test("an Artwork with only SVG Logo, or none, is saved with no Logo") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Forgotten Tale (2015).mkv"),
            harness.deviceFile("Lost Pages S01E01.mkv"),
        )

        // When
        harness.lightSync()

        // Then
        logoOf("Forgotten Tale") shouldBe ""
        logoOf("Lost Pages") shouldBe ""

    }

    test("a Synchro complète refreshes the Logo") {

        // Given
        harness.settings.setDataLanguage(Language.ENGLISH)
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        harness.settings.setDataLanguage(Language.FRENCH)

        // When
        harness.fullSync()

        // Then
        logoOf("Inception") shouldBe "/inception-fr-best.png"

    }

    test("the Unknown Artwork is never searched for a Logo") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Holidays in Brittany.mkv"))

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).single { it.isUnknown }.logoPath shouldBe null
        harness.tmdb.callCount("movie/{id}/images") shouldBe 0
        harness.tmdb.callCount("tv/{id}/images") shouldBe 0

    }

})
