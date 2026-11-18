package com.mskd.flux.features.player.domain.playback

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.Failed
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.Launched
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.NoPlayerInstalled
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult.PermissionDenied
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private class FakeExternalPlayerAdapter(var result: ExternalLaunchResult = Launched) : ExternalPlayerAdapter {
    val launched = mutableListOf<Media>()
    override fun launch(media: Media): ExternalLaunchResult {
        launched += media
        return result
    }
}

class PlaybackLaunchTest : FunSpec({

    fluxExtensions()

    lateinit var settings: SettingsDataStore
    lateinit var adapter: FakeExternalPlayerAdapter
    lateinit var saveProgress: SaveProgressUseCase
    lateinit var playback: PlaybackLaunch

    val movie = MediaMockups.movie
    val episode = MediaMockups.episode1

    beforeTest {
        settings = mockk(relaxed = true)
        coEvery { settings.externalPlayerIsEnabled() } returns true
        adapter = FakeExternalPlayerAdapter()
        saveProgress = mockk(relaxed = true)
        playback = PlaybackLaunch(settings = settings, externalPlayer = adapter, saveProgress = saveProgress)
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
            coVerify(exactly = 0) { saveProgress(any(), any(), any()) }
        }

    }

    context("result") {

        test("the position is recorded on the launched Media") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(5000L)
            coVerify(exactly = 1) { saveProgress(media = movie, progress = 5000L) }
        }

        test("a null position records nothing") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(null)
            coVerify(exactly = 0) { saveProgress(any(), any(), any()) }
        }

        test("a result without launch records nothing") {
            playback.onExternalResult(5000L)
            coVerify(exactly = 0) { saveProgress(any(), any(), any()) }
        }

        test("a new launch replaces the pending one") {
            playback.play(movie, forceInternal = false)
            playback.play(episode, forceInternal = false)
            playback.onExternalResult(5000L)
            coVerify(exactly = 1) { saveProgress(media = episode, progress = 5000L) }
            coVerify(exactly = 0) { saveProgress(media = movie, progress = any()) }
        }

        test("a result is recorded at most once") {
            playback.play(movie, forceInternal = false)
            playback.onExternalResult(5000L)
            playback.onExternalResult(6000L)
            coVerify(exactly = 1) { saveProgress(any(), any(), any()) }
        }

    }

})
