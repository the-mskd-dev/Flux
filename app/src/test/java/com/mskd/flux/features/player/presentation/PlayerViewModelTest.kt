package com.mskd.flux.features.player.presentation

import androidx.media3.common.Player
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.language.Language
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.features.player.data.PipIsEnabledUseCase
import com.mskd.flux.features.player.domain.manager.PlayerManager
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.player.domain.usecase.GetChaptersUseCase
import com.mskd.flux.features.player.domain.usecase.GetNextEpisodeUseCase
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import com.mskd.flux.features.player.fake.PlayerTestCases
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PlayerMockups
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
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
    lateinit var recordProgress: SaveProgressUseCase
    lateinit var playerManager: PlayerManager<Player>
    lateinit var player: Player
    lateinit var pipIsEnabledUseCase: PipIsEnabledUseCase
    lateinit var getSubtitlesUseCase: GetSubtitlesUseCase
    lateinit var saveTrackLanguageUseCase: SaveTrackLanguageUseCase
    lateinit var getChaptersUseCase: GetChaptersUseCase
    lateinit var getNextEpisodeUseCase: GetNextEpisodeUseCase

    fun updateVm(mediaId: Long = MediaMockups.episode1.mediaId) {

        val media = MediaMockups.allMedias.find { it.mediaId == mediaId }
        media?.let { observeArtworkUseCase(it.artworkId) }
        val playerParams = PlayerParams(
            mediaId = mediaId,
            artworkId = media?.artworkId
        )

        viewModel = PlayerViewModel(
            params = playerParams,
            observeArtworkUseCase = observeArtworkUseCase,
            settingsDataStore = settingsDataStore,
            playerManager = playerManager,
            pipIsEnabledUseCase = pipIsEnabledUseCase,
            saveProgressUseCase = recordProgress,
            getSubtitlesUseCase = getSubtitlesUseCase,
            saveTrackLanguageUseCase = saveTrackLanguageUseCase,
            getChaptersUseCase = getChaptersUseCase,
            getNextEpisodeUseCase = getNextEpisodeUseCase,
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
        recordProgress = mockk(relaxed = true)
        getChaptersUseCase = mockk(relaxed = true)
        coEvery { getChaptersUseCase(any()) } returns emptyList()
        getNextEpisodeUseCase = mockk(relaxed = true)
        coEvery { getNextEpisodeUseCase(any()) } returns null
        coEvery { getNextEpisodeUseCase(MediaMockups.episode1) } returns MediaMockups.episode2
        saveTrackLanguageUseCase = mockk(relaxed = true) {
            coEvery { getAudioLanguage() } returns Language.ENGLISH.code
            coEvery { getSubtitlesLanguage() } returns Language.ENGLISH.code
        }
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
                every { progress } returns MutableStateFlow(testCase.time)
            }

            updateVm(mediaId = testCase.media.mediaId)

            viewModel.uiState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.SaveTime)

                // Then
                coVerify { recordProgress(testCase.media, testCase.time) }

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
                coVerify { saveTrackLanguageUseCase(track = testCase.track) }

            }

        }

    }

    context("chapter button tap") {

        test("skip seeks to the end of the chapter") {

            // Given
            val openingChapter = VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING)

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(
                    PlayerIntent.OnChapterButtonTap(PlayerUiContent.ChapterButton.Skip(chapter = openingChapter))
                )

                // Then
                coVerify { playerManager.seekTo(progress = openingChapter.end) }

                cancelAndIgnoreRemainingEvents()
            }

        }

        test("next episode saves the time and changes the media") {

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(
                    PlayerIntent.OnChapterButtonTap(
                        PlayerUiContent.ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
                    )
                )

                // Then
                val state = awaitItem()
                state.content.media.mediaId shouldBe MediaMockups.episode2.mediaId
                state.content.chapterButton shouldBe null
                coVerify { recordProgress(MediaMockups.episode1, any()) }

                cancelAndIgnoreRemainingEvents()
            }

        }

        test("null button does nothing") {

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.OnChapterButtonTap(null))

                // Then
                expectNoEvents()
                coVerify(exactly = 0) { playerManager.seekTo(any()) }
                coVerify(exactly = 0) { recordProgress(any(), any()) }

                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    context("chapter button from progress") {

        withData(
            nameFn = { it.description },
            PlayerTestCases.ChapterButtonTest(
                description = "progress in opening chapter shows the Skip button",
                chapters = listOf(
                    VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING),
                ),
                progress = 30_000L,
                expectedChapterButton = PlayerUiContent.ChapterButton.Skip(
                    chapter = VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING)
                )
            ),
            PlayerTestCases.ChapterButtonTest(
                description = "progress in a non-final ending chapter shows the Skip button",
                chapters = listOf(
                    VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING),
                    VideoChapter(index = 1, start = 90_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER),
                    VideoChapter(index = 2, start = 1_200_000L, end = 1_300_000L, type = VideoChapterType.ENDING),
                    VideoChapter(index = 3, start = 1_300_000L, end = 1_380_000L, type = VideoChapterType.CHAPTER),
                ),
                progress = 1_250_000L,
                expectedChapterButton = PlayerUiContent.ChapterButton.Skip(
                    chapter = VideoChapter(index = 2, start = 1_200_000L, end = 1_300_000L, type = VideoChapterType.ENDING)
                )
            ),
            PlayerTestCases.ChapterButtonTest(
                description = "progress in the final ending chapter shows the NextEpisode button",
                chapters = listOf(
                    VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING),
                    VideoChapter(index = 1, start = 90_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER),
                    VideoChapter(index = 2, start = 1_200_000L, end = 1_380_000L, type = VideoChapterType.ENDING),
                ),
                progress = 1_300_000L,
                expectedChapterButton = PlayerUiContent.ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
            ),
            PlayerTestCases.ChapterButtonTest(
                description = "progress near the end of the media shows the NextEpisode button",
                chapters = emptyList(),
                progress = 1_370_000L,
                expectedChapterButton = PlayerUiContent.ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
            ),
            PlayerTestCases.ChapterButtonTest(
                description = "progress in a normal chapter hides the button",
                chapters = listOf(
                    VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING),
                    VideoChapter(index = 1, start = 90_000L, end = 1_380_000L, type = VideoChapterType.CHAPTER),
                ),
                progress = 100_000L,
                expectedChapterButton = null
            ),
        ) { testCase ->

            // Given
            getChaptersUseCase = mockk(relaxed = true)
            coEvery { getChaptersUseCase(any()) } returns testCase.chapters

            val progressFlow = MutableStateFlow(0L)
            playerManager = mockk(relaxed = true) {
                every { flow } returns MutableStateFlow(
                    PlayerManager.State.Ready(
                        player = player,
                        duration = MediaMockups.episode1.duration.minToMs
                    )
                )
                every { progress } returns progressFlow
            }

            updateVm()

            viewModel.uiState.test {

                // Skip initial state & ensure the media (and so the chapters) are loaded
                awaitItem()
                coVerify { playerManager.playMedia(any(), any()) }

                // When
                progressFlow.value = testCase.progress

                // Then
                if (testCase.expectedChapterButton == null) {
                    // No new state is emitted when the button stays/becomes null
                    viewModel.uiState.value.content.chapterButton shouldBe null
                } else {
                    val state = awaitItem()
                    state.content.chapterButton shouldBe testCase.expectedChapterButton
                }

                cancelAndIgnoreRemainingEvents()
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
            every { progress } returns MutableStateFlow(2000L)
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.GoToBackground)

            coVerify { playerManager.pause() }
            coVerify { recordProgress(any(), 2000L) }
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
            every { progress } returns MutableStateFlow(3000L)
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.GoToBackground)
            coVerify { playerManager.pause() }
            coVerify { recordProgress(any(), 3000L) }

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

    test("GoToBackground when paused saves time but does not ic_pause") {
        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PlayerManager.State.Ready(
                    player = player,
                    isPlaying = false,
                    duration = 10000L
                )
            )
            every { progress } returns MutableStateFlow(2000L)
        }
        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.GoToBackground)

            coVerify(exactly = 0) { playerManager.pause() }
            coVerify { recordProgress(any(), 2000L) }
        }
    }

    test("select track exception safety") {
        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
            coEvery { setAudioLanguage(any()) } throws RuntimeException("Mock database write failure")
        }
        saveTrackLanguageUseCase = SaveTrackLanguageUseCase(settings = settingsDataStore)
        updateVm()

        viewModel.uiState.test {
            awaitItem()
            val track = PlayerTrack(id = "1", label = "English", language = Language.ENGLISH.code, type = PlayerTrack.Type.AUDIO)

            // Should catch and not crash
            viewModel.handleIntent(PlayerIntent.SelectTrack(track))
        }
    }

})

private val PlayerUiState<Player>.content: PlayerUiContent<Player>
    get() = (state as State.Content).content