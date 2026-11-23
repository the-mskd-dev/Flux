package com.mskd.flux.features.player.domain.playback

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.InMemoryPreferencesDataStore
import com.mskd.flux.features.player.data.PendingLaunchStoreImpl
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.Failed
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.Launched
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.NoPlayerInstalled
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.PermissionDenied
import com.mskd.flux.features.progress.domain.ProgressTracker
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

private class FakeExternalPlayerAdapter(var result: ExternalLaunchResult = Launched) : ExternalPlayerAdapter {
    val launched = mutableListOf<Media>()
    var onLaunch: (Media) -> Unit = {}
    override fun launch(media: Media): ExternalLaunchResult {
        launched += media
        onLaunch(media)
        return result
    }
}

class PlaybackLaunchTest : FunSpec({

    fluxExtensions()

    lateinit var settings: SettingsDataStore
    lateinit var adapter: FakeExternalPlayerAdapter
    lateinit var progressTracker: ProgressTracker
    lateinit var database: DatabaseRepository
    lateinit var store: PendingLaunchStore
    lateinit var playback: PlaybackLaunch

    fun newModule() = PlaybackLaunch(
        settings = settings,
        externalPlayer = adapter,
        progressTracker = progressTracker,
        pendingLaunchStore = store,
        database = database,
    )

    val movie = MediaMockups.movie
    val episode = MediaMockups.episode1

    beforeTest {
        settings = mockk(relaxed = true)
        coEvery { settings.externalPlayerIsEnabled() } returns true
        adapter = FakeExternalPlayerAdapter()
        progressTracker = mockk(relaxed = true)
        store = PendingLaunchStoreImpl(InMemoryPreferencesDataStore())
        database = mockk()
        coEvery { database.getMovie(any()) } returns null
        coEvery { database.getEpisodes(any()) } returns emptyList()
        coEvery { database.getMovie(movie.artworkId) } returns movie
        coEvery { database.getEpisodes(episode.artworkId) } returns listOf(episode)
        playback = newModule()
    }

    context("decision") {

        test("an Unavailable Media starts no player") {
            playback.play(movie.copy(isAvailable = false), forceInternal = false) shouldBe PlaybackOutcome.Unavailable
            adapter.launched shouldBe emptyList()
        }

        test("external player enabled: handed to the External player") {
            playback.play(movie, forceInternal = false) shouldBe PlaybackOutcome.HandedToExternalPlayer
            adapter.launched shouldBe listOf(movie)
        }

        test("external player disabled: Internal player") {
            coEvery { settings.externalPlayerIsEnabled() } returns false
            playback.play(movie, forceInternal = false) shouldBe PlaybackOutcome.OpenInternalPlayer(movie)
            adapter.launched shouldBe emptyList()
        }

        test("forced internal: Internal player even when the external one is enabled") {
            playback.play(movie, forceInternal = true) shouldBe PlaybackOutcome.OpenInternalPlayer(movie)
            adapter.launched shouldBe emptyList()
        }

    }

    context("fallback") {

        withData(
            nameFn = { it.toString() },
            NoPlayerInstalled, PermissionDenied, Failed(RuntimeException("boom")), Failed(),
        ) { failure ->
            adapter.result = failure
            playback.play(movie, forceInternal = false) shouldBe PlaybackOutcome.OpenInternalPlayer(movie)
            adapter.launched shouldBe listOf(movie) // the external player is not retried
        }

        test("a failed launch leaves nothing pending") {
            adapter.result = NoPlayerInstalled
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(1000L)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
        }

    }

    context("result") {

        test("the position is recorded on the launched Media") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(5000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(media = movie, position = 5000L) }
        }

        test("a null position records nothing") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(null)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("a result without launch records nothing") {
            playback.onExternalResult(5000L)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("a new launch replaces the pending one") {
            playback.play(movie, forceInternal = false)
            playback.play(episode, forceInternal = false)
            playback.onExternalResult(5000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(media = episode, position = 5000L) }
            coVerify(exactly = 0) { progressTracker.recordProgress(media = movie, position = any()) }
        }

        test("known limit: Media A then Media B launched, one result is recorded on B, once") {
            playback.play(movie, forceInternal = false)
            playback.play(episode, forceInternal = false)
            playback.onExternalResult(5000L)
            playback.onExternalResult(6000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(any(), any(), any()) }
            coVerify(exactly = 1) { progressTracker.recordProgress(media = episode, position = 5000L) }
        }

        test("a result is recorded at most once") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(5000L)
            playback.onExternalResult(6000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(any(), any(), any()) }
        }

    }

    context("process death") {

        test("a result after a restart is recorded on the persisted Media") {
            playback.play(movie, forceInternal = false)
            val restarted = newModule() // Flux was killed while the External player was open
            restarted.onExternalResult(5000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(media = movie, position = 5000L) }
        }

        test("an Episode launched before a restart is found again") {
            playback.play(episode, forceInternal = false)
            newModule().onExternalResult(5000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(media = episode, position = 5000L) }
        }

        test("a result after a restart with no pending launch records nothing") {
            newModule().onExternalResult(5000L)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("a result after a restart is recorded at most once") {
            playback.play(movie, forceInternal = false)
            val restarted = newModule()
            restarted.onExternalResult(5000L)
            restarted.onExternalResult(6000L)
            newModule().onExternalResult(7000L)
            coVerify(exactly = 1) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("a persisted Media that no longer exists records nothing and clears the entry") {
            playback.play(movie, forceInternal = false)
            coEvery { database.getMovie(movie.artworkId) } returns null
            newModule().onExternalResult(5000L)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
            store.take() shouldBe null
        }

        test("a play after a restart clears the persisted launch of the previous run") {
            playback.play(movie, forceInternal = false)
            val restarted = newModule()
            restarted.play(episode, forceInternal = true)
            restarted.onExternalResult(5000L)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("a failed launch clears the persisted launch") {
            adapter.result = NoPlayerInstalled
            playback.play(movie, forceInternal = false)
            store.take() shouldBe null
        }

        test("a failure after a restart falls back to the Internal player on the persisted Media") {
            playback.play(movie, forceInternal = false)
            val restarted = newModule()
            restarted.onExternalFailure()
            restarted.internalPlayerRequests.first() shouldBe movie
        }

        test("cancelling the caller while a result is handled still records the Progress") {
            playback.play(movie, forceInternal = false)
            val lookupStarted = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { database.getMovie(movie.artworkId) } coAnswers {
                lookupStarted.complete(Unit)
                release.await()
                movie
            }
            val caller = launch(Dispatchers.Default) { playback.onExternalResult(5000L) }
            lookupStarted.await()
            caller.cancel() // Activity destroyed after the pending launch was consumed
            release.complete(Unit)
            caller.join()
            coVerify(exactly = 1) { progressTracker.recordProgress(media = movie, position = 5000L) }
            playback.onExternalResult(6000L) // and a retry cannot record it twice
            coVerify(exactly = 1) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("the persisted launch is cleared once consumed") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(5000L)
            store.take() shouldBe null
        }

    }

    context("pending launch lifecycle") {

        suspend fun lostResultThen(next: suspend () -> Unit) {
            playback.play(movie, forceInternal = false) // handed off, result never comes back
            next()
            playback.onExternalResult(9000L) // late result
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
        }

        test("a stale launch is cleared by a later fallback") {
            lostResultThen {
                adapter.result = NoPlayerInstalled
                playback.play(episode, forceInternal = false)
            }
        }

        test("a stale launch is cleared by a later forced-internal play") {
            lostResultThen { playback.play(episode, forceInternal = true) }
        }

        test("a stale launch is cleared by a later Unavailable play") {
            lostResultThen { playback.play(episode.copy(isAvailable = false), forceInternal = false) }
        }

        test("a stale launch is cleared by a later internal play") {
            lostResultThen {
                coEvery { settings.externalPlayerIsEnabled() } returns false
                playback.play(episode, forceInternal = false)
            }
        }

        test("a result delivered from inside launch is recorded on the launched Media") {
            adapter.onLaunch = { runBlocking { playback.onExternalResult(7000L) } }
            playback.play(movie, forceInternal = false)
            coVerify(exactly = 1) { progressTracker.recordProgress(media = movie, position = 7000L) }
        }

    }

    context("failure after launch") {

        test("falls back to the Internal player on the launched Media, without retrying the External one") {
            playback.play(movie, forceInternal = false)
            playback.onExternalFailure(RuntimeException("crash"))
            playback.internalPlayerRequests.first() shouldBe movie
            adapter.launched shouldBe listOf(movie)
        }

        test("clears the pending launch: a later result records nothing and the fallback happens once") {
            playback.play(movie, forceInternal = false)
            playback.onExternalFailure()
            playback.onExternalFailure()
            playback.onExternalResult(5000L)
            coVerify(exactly = 0) { progressTracker.recordProgress(any(), any(), any()) }
            playback.internalPlayerRequests.first() shouldBe movie
            withTimeoutOrNull(100) { playback.internalPlayerRequests.first() } shouldBe null
        }

        test("a failure with no pending launch requests nothing") {
            playback.onExternalFailure()
            withTimeoutOrNull(100) { playback.internalPlayerRequests.first() } shouldBe null
        }

        test("a plain result (user cancel, no position) requests no fallback") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(null)
            withTimeoutOrNull(100) { playback.internalPlayerRequests.first() } shouldBe null
        }

    }

})
