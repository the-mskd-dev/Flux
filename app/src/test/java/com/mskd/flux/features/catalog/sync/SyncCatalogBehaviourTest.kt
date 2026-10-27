package com.mskd.flux.features.catalog.sync

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.longs.shouldBeGreaterThan as shouldBeGreaterThanLong
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/**
 * Behaviour of the Synchro, observed through the Catalogue in database, the successive Synchro
 * states and the TMDB calls. See docs/specs/sync-catalog.md.
 */
class SyncCatalogBehaviourTest : FunSpec({

    lateinit var harness: SyncHarness

    beforeTest { harness = SyncHarness() }
    afterTest { harness.close() }

    test("the first Synchro fetches and saves every file of the Sources") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S01E02.mkv"),
        )

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")
        harness.catalog.getSeasons().map { it.title } shouldContainExactlyInAnyOrder listOf("Season 1")
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Pilot", "Cat's in the Bag...")
        harness.catalog.getMedias().filterIsInstance<Movie>().single().duration shouldBe 148
        harness.catalog.getMedias().filterIsInstance<Episode>().map { it.artworkId }.distinct() shouldBe listOf(1396L)

    }

    test("a Synchro légère only fetches the new files") {

        // Given
        val inception = harness.deviceFile("Inception (2010).mkv")
        harness.deviceStorage.availableFiles = listOf(inception)
        harness.lightSync()

        harness.deviceStorage.availableFiles = listOf(inception, harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.tmdb.resetCalls()

        // When
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/movie") shouldBe 0
        harness.tmdb.callCount("movie/{id}") shouldBe 0
        harness.tmdb.callCount("search/tv") shouldBe 1
        harness.tmdb.callCount("tv/{id}/season/{season}") shouldBe 1
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Pilot")

    }

    test("a Synchro complète fetches every file again and refreshes the genres") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        harness.tmdb.resetCalls()

        // When
        harness.fullSync()

        // Then
        harness.tmdb.callCount("search/movie") shouldBe 1
        harness.tmdb.callCount("movie/{id}") shouldBe 1
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        harness.tmdb.callCount("genre/tv/list") shouldBe 1
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")

    }

    test("a file that TMDB cannot identify is saved as a Fichier non reconnu and is not fetched again") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Holidays in Brittany.mkv"))

        // When
        harness.lightSync()
        harness.tmdb.resetCalls()
        harness.lightSync()

        // Then
        val media = harness.catalog.getMedias().single()
        media.shouldBeInstanceOf<Episode>()
        media.artworkId shouldBe Artwork.UNKNOWN_ID
        media.file.name shouldBe "Holidays in Brittany.mkv"
        harness.tmdb.callCount("search/{type}") shouldBe 0

    }

    test("a file whose search fails because of the network is not saved and is fetched again by the next Synchro légère") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.cutNetwork()

        // When
        harness.lightSync()

        // Then
        harness.catalog.getMedias() shouldBe emptyList()
        harness.catalog.getArtworks(includePrivates = true) shouldBe emptyList()
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 1)

        // When
        harness.tmdb.restoreNetwork()
        harness.lightSync()

        // Then
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 0)

    }

    test("a file is a Fichier en échec, not a Fichier non reconnu, when no TMDB token is available") {

        // Given
        harness.close()
        harness = SyncHarness(tmdbToken = "")
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))

        // When
        harness.lightSync()

        // Then
        harness.catalog.getMedias() shouldBe emptyList()
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 1)

    }

    test("a failure while fetching the details of one Artwork does not prevent the others from being saved") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.tmdb.failEndpoint("movie/{id}")

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Breaking Bad")
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Pilot")
        harness.states.last() shouldBe SyncState.Idle(failedFiles = 1)

    }

    test("a failure while reading the duration of a Média does not prevent the others from being saved") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S01E02.mkv"),
        )
        harness.unreadableDurations += "Breaking Bad S01E01.mkv"

        // When
        harness.lightSync()

        // Then
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Pilot", "Cat's in the Bag...")

    }

    test("the state goes from Idle to Syncing and back to Idle") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))

        // When
        harness.lightSync()

        // Then
        harness.states.first() shouldBe SyncState.Idle()
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(false)
        harness.states.last() shouldBe SyncState.Idle()

    }

    test("a Synchro légère requested during a Synchro complète is ignored") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.pause()
        harness.requestFullSync()

        // When
        harness.requestLightSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(true)
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")

    }

    test("a Synchro complète requested during a Synchro légère replaces it") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.tmdb.pause()
        harness.requestLightSync()

        // When
        harness.requestFullSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.states.filterIsInstance<SyncState.Syncing>().map { it.full }.distinct() shouldBe listOf(false, true)
        harness.tmdb.callCount("genre/movie/list") shouldBe 1
        // The cancelled Synchro légère never got further than its search
        harness.tmdb.callCount("movie/{id}") shouldBe 1
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")

    }

    test("an Artwork privé stays private after a Synchro complète") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        harness.catalog.setArtworkPrivate(artworkId = 27205L, isPrivate = true)

        // When
        harness.fullSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).associate { it.title to it.isPrivate } shouldBe mapOf(
            "Inception" to true,
            "Breaking Bad" to false,
        )

    }

    test("the playback progress and the status of a Média are kept after a Synchro légère and a Synchro complète") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.lightSync()
        harness.catalog.saveMedias(harness.catalog.getMedias().map { it.withProgress(time = 5_000L, status = Status.IS_WATCHING) })

        // When
        harness.deviceStorage.availableFiles += harness.deviceFile("Breaking Bad S01E02.mkv")
        harness.lightSync()
        val afterLight = harness.catalog.getMedias().filter { it.title != "Cat's in the Bag..." }
        harness.fullSync()

        // Then
        afterLight.map { it.currentTime to it.status }.distinct() shouldBe listOf(5_000L to Status.IS_WATCHING)
        harness.catalog.getMedias().filter { it.title != "Cat's in the Bag..." }
            .map { it.currentTime to it.status }.distinct() shouldBe listOf(5_000L to Status.IS_WATCHING)

    }

    test("the playback progress follows the file when a Synchro complète attaches it to another Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        val movie = harness.catalog.getMedias().single() as Movie
        // The file was previously identified as another Artwork
        harness.catalog.saveMedias(listOf(movie.copy(artworkId = 999L, currentTime = 7_000L, status = Status.IS_WATCHING)))

        // When
        harness.fullSync()

        // Then
        val result = harness.catalog.getMedias().single()
        result.artworkId shouldBe 27205L
        result.currentTime shouldBe 7_000L
        result.status shouldBe Status.IS_WATCHING

    }

    test("NSFW Artworks are moved to the private folder when it includes them") {

        // Given
        harness.privateFolder.setEnabled(true)
        harness.privateFolder.setIncludeNsfw(true)
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Forbidden Film (2020).mkv"),
        )

        // When
        harness.lightSync()

        // Then
        harness.catalog.getArtworks(includePrivates = true).associate { it.title to it.isPrivate } shouldBe mapOf(
            "Inception" to false,
            "Forbidden Film" to true,
        )

    }

    test("a Fichier manquant of an available Source is deleted by a Synchro légère") {

        // Given
        val inception = harness.deviceFile("Inception (2010).mkv")
        val pilot = harness.deviceFile("Breaking Bad S01E01.mkv")
        harness.deviceStorage.availableFiles = listOf(inception, pilot)
        harness.lightSync()

        // When
        harness.deviceStorage.availableFiles = listOf(inception)
        harness.lightSync()

        // Then
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception")
        harness.catalog.getSeasons() shouldBe emptyList()

    }

    test("a Fichier manquant of an available Source is deleted by a Synchro complète") {

        // Given
        val inception = harness.deviceFile("Inception (2010).mkv")
        val pilot = harness.deviceFile("Breaking Bad S01E01.mkv")
        harness.deviceStorage.availableFiles = listOf(inception, pilot)
        harness.lightSync()

        // When
        harness.deviceStorage.availableFiles = listOf(pilot)
        harness.fullSync()

        // Then
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Pilot")
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Breaking Bad")

    }

    test("the files of a Source indisponible are kept by a Synchro légère") {

        // Given
        val root = "content://custom/tree/sd"
        val pilot = harness.customFile(root, "Breaking Bad S01E01.mkv")
        val source = harness.addCustomSource(root = root, files = listOf(pilot))
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()

        // When
        source.available = false
        harness.deviceStorage.availableFiles = emptyList()
        harness.lightSync()

        // Then
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Pilot")
        harness.catalog.getSeasons().map { it.title } shouldContainExactlyInAnyOrder listOf("Season 1")

    }

    test("the files of a Source indisponible are kept by a Synchro complète") {

        // Given
        val root = "content://custom/tree/sd"
        val pilot = harness.customFile(root, "Breaking Bad S01E01.mkv")
        val source = harness.addCustomSource(root = root, files = listOf(pilot))
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()

        // When
        source.available = false
        harness.fullSync()

        // Then
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Pilot")
        harness.catalog.getSeasons().map { it.title } shouldContainExactlyInAnyOrder listOf("Season 1")
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")

    }

    test("the progress never goes backwards and only reaches 100 % when the Synchro ends") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
            harness.deviceFile("Breaking Bad S01E02.mkv"),
            harness.deviceFile("Breaking Bad S02E01.mkv"),
            harness.deviceFile("Holidays in Brittany.mkv"),
        )

        // When
        harness.fullSync()

        // Then
        val progresses = harness.states.filterIsInstance<SyncState.Syncing>().map { it.progress }
        progresses.zipWithNext().all { (previous, next) -> next >= previous } shouldBe true
        progresses.distinct().size shouldBeGreaterThan 1
        progresses.dropLast(1).all { it < 1f } shouldBe true
        harness.states.last().shouldBeInstanceOf<SyncState.Idle>()

    }

    test("a new movie costs exactly one search and one details call") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))

        // When
        harness.lightSync()

        // Then
        harness.tmdb.callCount("search/movie") shouldBe 1
        harness.tmdb.callCount("movie/{id}") shouldBe 1
        harness.tmdb.totalCallCount shouldBe 2

    }

    test("a new show costs a single show details call and no separate translation call") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))

        // When
        harness.lightSync()

        // Then
        harness.tmdb.callCount("tv/{id}") shouldBe 1
        harness.tmdb.callCount("tv/{id}/translations") shouldBe 0
        harness.tmdb.callCount("tv/{id}/credits") shouldBe 0

    }

    test("an empty title and description in the user language are completed by English") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Forgotten Tale (2015).mkv"))

        // When
        harness.lightSync()

        // Then
        val artwork = harness.catalog.getArtworks(includePrivates = true).single()
        artwork.title shouldBe "Forgotten Tale"
        artwork.description shouldBe "A tale nobody translated."
        val movie = harness.catalog.getMedias().single()
        movie.title shouldBe "Forgotten Tale"
        movie.description shouldBe "A tale nobody translated."
        harness.tmdb.callCount("movie/{id}/translations") shouldBe 0

    }

    test("the credits of new Artworks are in database at the end of the Synchro without a separate credits call") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )

        // When
        harness.lightSync()

        // Then
        harness.people.flowPeople(artworkId = 27205L).first().map { it.name } shouldContainExactlyInAnyOrder listOf("Leonardo DiCaprio", "Christopher Nolan")
        harness.people.flowPeople(artworkId = 1396L).first().map { it.name } shouldContainExactlyInAnyOrder listOf("Bryan Cranston")
        harness.tmdb.callCount("movie/{id}/credits") shouldBe 0
        harness.tmdb.callCount("tv/{id}/credits") shouldBe 0

    }

    test("the credits that TMDB no longer returns are removed by the next Synchro of the Artwork") {

        // Given
        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Inception (2010).mkv"))
        harness.lightSync()
        harness.people.savePeople(
            listOf(Person(id = 1, creditId = "stale", artworkId = 27205L, name = "Former Actor", profilePath = null, role = "Cobb", type = PersonType.CAST))
        )

        // When
        harness.fullSync()

        // Then
        harness.people.flowPeople(artworkId = 27205L).first().map { it.name } shouldContainExactlyInAnyOrder listOf("Leonardo DiCaprio", "Christopher Nolan")

    }

    test("the Synchro time and the version code are saved even when there is no new file") {

        // When
        harness.lightSync()

        // Then
        harness.user.getSyncTime() shouldBeGreaterThanLong 0L
        harness.user.getVersionCode() shouldBe 1

    }

    test("an Artwork is saved as soon as its folder is fetched, before the end of the Synchro") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.tmdb.pause("tv/{id}/season/{season}")

        // When
        harness.requestLightSync()

        // Then
        withTimeout(10.seconds) {
            while (harness.catalog.getArtworks(includePrivates = true).isEmpty()) delay(10)
        }
        harness.states.last().shouldBeInstanceOf<SyncState.Syncing>()
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldBe listOf("Inception")
        harness.catalog.getMedias().map { it.title } shouldBe listOf("Inception")

        harness.tmdb.resume()
        harness.awaitIdle()
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")

    }

    test("the folders already saved are kept when a Synchro complète cancels a Synchro légère") {

        // Given
        harness.deviceStorage.availableFiles = listOf(
            harness.deviceFile("Inception (2010).mkv"),
            harness.deviceFile("Breaking Bad S01E01.mkv"),
        )
        harness.tmdb.pause("tv/{id}/season/{season}")
        harness.requestLightSync()
        withTimeout(10.seconds) {
            while (harness.catalog.getArtworks(includePrivates = true).isEmpty()) delay(10)
        }

        // When
        harness.requestFullSync()
        harness.tmdb.resume()
        harness.awaitIdle()

        // Then
        harness.catalog.getArtworks(includePrivates = true).map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Breaking Bad")
        harness.catalog.getMedias().map { it.title } shouldContainExactlyInAnyOrder listOf("Inception", "Pilot")

    }

    test("a Fichier manquant is deleted even if the rest of the Synchro does not end") {

        // Given
        val inception = harness.deviceFile("Inception (2010).mkv")
        harness.deviceStorage.availableFiles = listOf(inception)
        harness.lightSync()

        harness.deviceStorage.availableFiles = listOf(harness.deviceFile("Breaking Bad S01E01.mkv"))
        harness.tmdb.pause()

        // When
        harness.requestLightSync()

        // Then
        withTimeout(10.seconds) {
            while (harness.catalog.getArtworks(includePrivates = true).isNotEmpty()) delay(10)
        }
        harness.catalog.getMedias() shouldBe emptyList()

        harness.tmdb.resume()
        harness.awaitIdle()

    }

})

private fun Media.withProgress(time: Long, status: Status): Media = when (this) {
    is Movie -> copy(currentTime = time, status = status)
    is Episode -> copy(currentTime = time, status = status)
}
