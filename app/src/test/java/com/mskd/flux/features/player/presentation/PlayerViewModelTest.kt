package com.mskd.flux.features.player.presentation

import androidx.media3.common.Player
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.features.player.data.PipIsEnabledUseCase
import com.mskd.flux.features.player.fake.PlayerTestCases
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PlayerMockups
import com.mskd.flux.platform.PlayerManager
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: PlayerViewModel<Player>
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var saveProgress: SaveProgressUseCase
    lateinit var playerManager: PlayerManager<Player>
    lateinit var player: Player
    lateinit var pipIsEnabledUseCase: PipIsEnabledUseCase
    lateinit var getSubtitlesUseCase: GetSubtitlesUseCase

    fun updateVm(mediaId: Long = MediaMockups.episode1.mediaId) {

        saveProgress = mockk(relaxed = true)

        val media = MediaMockups.allMedias.find { it.mediaId == mediaId }
        media?.let { observeArtworkUseCase(it.artworkId) }

        viewModel = PlayerViewModel(
            mediaId = mediaId,
            observeArtworkUseCase = observeArtworkUseCase,
            settingsDataStore = settingsDataStore,
            playerManager = playerManager,
            pipIsEnabledUseCase = pipIsEnabledUseCase,
            saveProgressUseCase = saveProgress,
            getSubtitlesUseCase = getSubtitlesUseCase
        )

    }

    beforeTest {

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        player = mockk(relaxed = true) {
            every { duration } returns 10000L
        }

        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(PlayerManager.State.Ready(player = player))
        }

        pipIsEnabledUseCase = mockk(relaxed = true)
        getSubtitlesUseCase = mockk(relaxed = true)
        observeArtworkUseCase = FakeObserveArtworkUseCase()

        updateVm()

    }

    test("interface visibility") {
        viewModel.uiState.test {

            // Hidden by default
            val initialState = awaitItem()
            initialState.content.showInterface shouldBe false

            // Test show
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
            val showedState = awaitItem()
            showedState.content.showInterface shouldBe true

            // Test hide
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
            val hiddenState = awaitItem()
            hiddenState.content.showInterface shouldBe false

        }
    }

    context("show settings") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.ShowSettings(
                description = "Show settings sheet",
                sheet = PlayerUiContent.SettingsSheet.Settings,
            ),
            PlayerTestCases.ShowSettings(
                description = "Show audio sheet",
                sheet = PlayerUiContent.SettingsSheet.Tracks(PlayerTrack.Type.AUDIO),
            ),
            PlayerTestCases.ShowSettings(
                description = "Show subtitles sheet",
                sheet = PlayerUiContent.SettingsSheet.Settings,
            )
        ) { testCase ->

            viewModel.uiState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.ShowSettings(sheet = testCase.sheet))

                // Then
                val settingsState = awaitItem()
                settingsState.content.settingsSheet shouldBe testCase.sheet

            }

        }
    }

    context("save time") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.SaveTime(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                time = MediaMockups.movie.duration.minToMs.times(0.5).toLong(),
            ),
            PlayerTestCases.SaveTime(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                time = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                time = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                time = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                time = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).toLong(),
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time for last episode at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                time = MediaMockups.episodes.lastEpisode.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
            )
        ) { testCase ->

            // Given
            observeArtworkUseCase(testCase.media.artworkId)
            updateVm(mediaId = testCase.media.mediaId)

            playerManager = mockk(relaxed = true) {
                every { flow } returns MutableStateFlow(PlayerManager.State.Ready(
                    player = player,
                    duration = testCase.media.duration.minToMs
                ))
                every { progress } returns MutableStateFlow(PlayerManager.Progress(
                    progress = testCase.time
                ))
            }

            updateVm(mediaId = testCase.media.mediaId)

            viewModel.uiState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.SaveTime)

                // Then
                coVerify { saveProgress(testCase.media, testCase.time) }

            }


        }
    }

    context("back tap") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.PlayerBackTap(
                description = "Back tap when interface is showed",
                interfaceShowed = true,
            ),
            PlayerTestCases.PlayerBackTap(
                description = "Back tap when interface is not showed",
                interfaceShowed = false,
            )
        ) { testCase ->

            viewModel.uiState.test {
                awaitItem()

                if (testCase.interfaceShowed) {
                    viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
                    awaitItem().content.showInterface shouldBe true
                }

                viewModel.event.test {
                    viewModel.handleIntent(PlayerIntent.OnBackTap)

                    if (testCase.interfaceShowed) {
                        awaitItem() shouldBe PlayerEvent.BackToPreviousScreen
                    } else {
                        expectNoEvents()
                    }
                }

                cancelAndIgnoreRemainingEvents()
            }

        }
    }

    test("toggle play button") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.TogglePlayButton)
            coVerify { playerManager.togglePlay() }
        }
    }

    test("fast rewind") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.handleIntent(PlayerIntent.OnFastRewind)

            val finalState = awaitItem()
            finalState.content.seekOverlay shouldBe PlayerUiContent.SeekOverlay(amount = state.content.playerRewind, type = PlayerUiContent.SeekOverlay.Type.REWIND)
            coVerify { playerManager.seekRewind(any()) }
        }
    }

    test("fast forward") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.handleIntent(PlayerIntent.OnFastForward)

            val finalState = awaitItem()
            finalState.content.seekOverlay shouldBe PlayerUiContent.SeekOverlay(amount = state.content.playerForward, type = PlayerUiContent.SeekOverlay.Type.FORWARD)
            coVerify { playerManager.seekForward(any()) }

        }
    }

    test("update progress") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.UpdateProgress(4L))

            coVerify { playerManager.seekTo(progress = 4L) }
        }
    }

    context("select track") {

        withData(
            nameFn = { it.description },
            PlayerTestCases.SelectTrack(
                description = "Select subtitle",
                track = PlayerMockups.Subtitles.english
            ),
            PlayerTestCases.SelectTrack(
                description = "Select audio",
                track = PlayerMockups.Audio.english
            ),
        ) { testCase ->

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(PlayerIntent.SelectTrack(testCase.track))

                coVerify { playerManager.selectTrack(track = testCase.track) }
                if (testCase.track.type == PlayerTrack.Type.SUBTITLES) {
                    coVerify { settingsDataStore.setSubtitlesLanguage(any()) }
                } else {
                    coVerify { settingsDataStore.setAudioLanguage(any()) }
                }

            }

        }

    }

    test("cancel next episode") {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.CancelNextEpisode)

            val state = awaitItem()
            state.content.nextButton.shouldBeInstanceOf<PlayerUiContent.NextButton.Canceled>()

        }

    }

    test("play next episode") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.PlayNextEpisode(MediaMockups.episode2))

            val state = awaitItem()
            state.content.media.shouldNotBeNull {
                mediaId shouldBe MediaMockups.episode2.mediaId
            }
        }
    }

    test("on volume change") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.OnVolumeChange(delta = .5f))

            val state = awaitItem()
            state.content.ambientOverlay?.type shouldBe PlayerUiContent.AmbientOverlay.Type.VOLUME
            coVerify { playerManager.changeVolume(.5f) }

        }
    }

    test("on brightness change") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.event.test {

                viewModel.handleIntent(PlayerIntent.OnBrightnessChange(delta = .5f))

                val event = awaitItem()

                event shouldBe PlayerEvent.ChangeBrightness(delta = .5f)

            }

        }
    }

    context("update ambient overlay") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.UpdateAmbientOverlay(
                description = "Brightness",
                type = PlayerUiContent.AmbientOverlay.Type.BRIGHTNESS,
                value = 50
            ),
            PlayerTestCases.UpdateAmbientOverlay(
                description = "Volume",
                type = PlayerUiContent.AmbientOverlay.Type.VOLUME,
                value = 50
            )
        ) { testCase ->
            viewModel.uiState.test {
                awaitItem()

                viewModel.handleIntent(PlayerIntent.UpdateAmbientOverlay(type = testCase.type, value = testCase.value))

                val state = awaitItem()
                state.content.ambientOverlay shouldBe PlayerUiContent.AmbientOverlay(type = testCase.type, value = testCase.value)
            }
        }
    }

    test("go to background when playing") {
        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PlayerManager.State.Ready(
                    player = player,
                    isPlaying = true,
                    duration = 10000L
                )
            )
            every { progress } returns MutableStateFlow(PlayerManager.Progress(
                progress = 2000L
            ))
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.GoToBackground)

            coVerify { playerManager.pause() }
            coVerify { saveProgress(any(), 2000L) }
        }
    }

    test("go to background and return to foreground") {
        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PlayerManager.State.Ready(
                    player = player,
                    isPlaying = true,
                    duration = 10000L
                )
            )
            every { progress } returns MutableStateFlow(PlayerManager.Progress(
                progress = 3000L
            ))
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.GoToBackground)
            coVerify { playerManager.pause() }
            coVerify { saveProgress(any(), 3000L) }

            viewModel.handleIntent(PlayerIntent.GoToForeground)
            coVerify { playerManager.play() }
        }
    }

    test("onCleared lifecycle disconnects player manager") {
        val method = viewModel.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)

        coVerify { playerManager.disconnect(any()) }
    }

    test("error state when artworkState is State.Error") {
        observeArtworkUseCase(-999L)
        updateVm(mediaId = -999L)

        viewModel.uiState.test {
            awaitItem().state shouldBe State.Error()
        }
    }

    test("error state when playerState is PlayerManager.State.Error") {
        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(PlayerManager.State.Error())
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem().state shouldBe State.Error()
        }
    }

    test("GoToBackground when paused saves time but does not pause") {
        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PlayerManager.State.Ready(
                    player = player,
                    isPlaying = false,
                    duration = 10000L
                )
            )
            every { progress } returns MutableStateFlow(PlayerManager.Progress(
                progress = 2000L
            ))
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.GoToBackground)

            coVerify(exactly = 0) { playerManager.pause() }
            coVerify { saveProgress(any(), 2000L) }
        }
    }

    test("select track exception safety") {
        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
            coEvery { setAudioLanguage(any()) } throws RuntimeException("Mock database write failure")
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()
            val track = PlayerTrack(id = "1", label = "English", language = "en", type = PlayerTrack.Type.AUDIO)

            // Should catch and not crash
            viewModel.handleIntent(PlayerIntent.SelectTrack(track))
        }
    }

})

private val PlayerUiState<Player>.content: PlayerUiContent<Player>
    get() = (state as State.Content).content