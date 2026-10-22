package com.mskd.flux.features.player.presentation

import androidx.media3.common.Player
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.FullArtwork
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
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

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
            artworkId = media?.artworkId ?: Artwork.UNKNOWN_ID
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
        viewModel.interfaceState.test {

            // Hidden by default
            awaitItem().showInterface shouldBe false

            // Test show
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
            awaitItem().showInterface shouldBe true

            // Test hide
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
            awaitItem().showInterface shouldBe false

        }
    }

    context("show settings") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.ShowSettings(
                description = "Show settings sheet",
                sheet = SettingsSheet.Settings,
            ),
            PlayerTestCases.ShowSettings(
                description = "Show audio sheet",
                sheet = SettingsSheet.Tracks(PlayerTrack.Type.AUDIO),
            ),
            PlayerTestCases.ShowSettings(
                description = "Show subtitles sheet",
                sheet = SettingsSheet.Settings,
            )
        ) { testCase ->

            viewModel.interfaceState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.ShowSettings(sheet = testCase.sheet))

                // Then
                awaitItem().settingsSheet shouldBe testCase.sheet

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

            viewModel.session.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.OnClosePiP)

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

            viewModel.interfaceState.test {
                awaitItem()

                if (testCase.interfaceShowed) {
                    viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
                    awaitItem().showInterface shouldBe true
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
        viewModel.overlays.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.OnFastRewind)

            awaitItem().seekOverlay shouldBe SeekOverlay(amount = viewModel.sessionContent.playerRewind, type = SeekOverlay.Type.REWIND)
            coVerify { playerManager.seekRewind(any()) }
        }
    }

    test("fast forward") {
        viewModel.overlays.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.OnFastForward)

            awaitItem().seekOverlay shouldBe SeekOverlay(amount = viewModel.sessionContent.playerForward, type = SeekOverlay.Type.FORWARD)
            coVerify { playerManager.seekForward(any()) }

        }
    }

    test("overlay change emits neither on session nor on interface") {
        viewModel.session.test {
            awaitItem()
            viewModel.interfaceState.test {
                awaitItem()
                viewModel.overlays.test {
                    awaitItem()

                    // When
                    viewModel.handleIntent(PlayerIntent.OnFastForward)
                    viewModel.handleIntent(PlayerIntent.UpdateAmbientOverlay(type = AmbientOverlay.Type.VOLUME, value = 30))

                    // Then
                    awaitItem().seekOverlay shouldBe SeekOverlay(amount = viewModel.sessionContent.playerForward, type = SeekOverlay.Type.FORWARD)
                    awaitItem().ambientOverlay shouldBe AmbientOverlay(type = AmbientOverlay.Type.VOLUME, value = 30)
                    cancelAndIgnoreRemainingEvents()
                }
                expectNoEvents()
            }
            expectNoEvents()
        }
    }

    test("play state change emits neither on session nor on interface") {
        val flow = MutableStateFlow<PlayerManager.State<Player>>(PlayerManager.State.Ready(player = player))
        every { playerManager.flow } returns flow
        updateVm()

        viewModel.session.test {
            awaitItem()
            viewModel.isPlaying.test {
                awaitItem() shouldBe false
                flow.value = PlayerManager.State.Ready(player = player, isPlaying = true)
                awaitItem() shouldBe true
            }
            expectNoEvents()
        }
    }

    test("seek to") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.SeekTo(4L))

            coVerify { playerManager.seekTo(progress = 4L) }
        }
    }

    test("artwork re-emission without media change does not emit a new state") {

        // Given
        val fullArtwork = FakeObserveArtworkUseCase()(MediaMockups.showArtwork.id).first() as State.Content
        val artworkFlow = MutableStateFlow<State<FullArtwork>>(fullArtwork)
        observeArtworkUseCase = object : ObserveArtworkUseCase {
            override fun invoke(artworkId: Long): Flow<State<FullArtwork>> = artworkFlow
        }
        updateVm()

        viewModel.session.test {
            awaitItem() shouldBe viewModel.session.value
            viewModel.session.value.shouldBeInstanceOf<State.Content<*>>()

            // When
            val show = fullArtwork.content as FullArtwork.FullShow
            artworkFlow.value = State.Content(show.copy(genres = persistentListOf()))

            // Then
            expectNoEvents()
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

            viewModel.session.test {

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

            viewModel.session.test {
                awaitItem()

                // When
                viewModel.handleIntent(
                    PlayerIntent.OnChapterButtonTap(ChapterButton.Skip(chapter = openingChapter))
                )

                // Then
                coVerify { playerManager.seekTo(progress = openingChapter.end) }

                cancelAndIgnoreRemainingEvents()
            }

        }

        test("next episode saves the time and changes the media") {

            viewModel.session.test {
                awaitItem()

                // When
                viewModel.handleIntent(
                    PlayerIntent.OnChapterButtonTap(
                        ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
                    )
                )

                // Then
                (awaitItem() as State.Content).content.media.mediaId shouldBe MediaMockups.episode2.mediaId
                viewModel.overlays.value.chapterButton shouldBe null
                coVerify { recordProgress(MediaMockups.episode1, any()) }

                cancelAndIgnoreRemainingEvents()
            }

        }

        test("null button does nothing") {

            viewModel.session.test {
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
                expectedChapterButton = ChapterButton.Skip(
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
                expectedChapterButton = ChapterButton.Skip(
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
                expectedChapterButton = ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
            ),
            PlayerTestCases.ChapterButtonTest(
                description = "progress near the end of the media shows the NextEpisode button",
                chapters = emptyList(),
                progress = 1_370_000L,
                expectedChapterButton = ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
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

            viewModel.overlays.test {

                // Skip initial state & ensure the media (and so the chapters) are loaded
                awaitItem()
                coVerify { playerManager.playMedia(any(), any()) }

                // When
                progressFlow.value = testCase.progress

                // Then
                if (testCase.expectedChapterButton == null) {
                    // No new state is emitted when the button stays/becomes null
                    viewModel.overlays.value.chapterButton shouldBe null
                } else {
                    awaitItem().chapterButton shouldBe testCase.expectedChapterButton
                }

                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("next episode is searched once per media despite several progress ticks") {

        // Given
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

        viewModel.overlays.test {
            awaitItem()
            coVerify { playerManager.playMedia(any(), any()) }

            // When
            progressFlow.value = 1_370_000L
            awaitItem().chapterButton shouldBe
                ChapterButton.NextEpisode(nextEpisode = MediaMockups.episode2)
            progressFlow.value = 1_371_000L
            progressFlow.value = 1_372_000L
            progressFlow.value = 1_373_000L
            viewModel.progress.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            // Then
            coVerify(exactly = 1) { getNextEpisodeUseCase(MediaMockups.episode1) }

            cancelAndIgnoreRemainingEvents()
        }

    }

    test("on volume change") {
        viewModel.overlays.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.OnVolumeChange(delta = .5f))

            awaitItem().ambientOverlay?.type shouldBe AmbientOverlay.Type.VOLUME
            coVerify { playerManager.changeVolume(.5f) }

        }
    }

    test("on brightness change") {
        viewModel.session.test {
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
                type = AmbientOverlay.Type.BRIGHTNESS,
                value = 50
            ),
            PlayerTestCases.UpdateAmbientOverlay(
                description = "Volume",
                type = AmbientOverlay.Type.VOLUME,
                value = 50
            )
        ) { testCase ->
            viewModel.overlays.test {
                awaitItem()

                viewModel.handleIntent(PlayerIntent.UpdateAmbientOverlay(type = testCase.type, value = testCase.value))

                awaitItem().ambientOverlay shouldBe AmbientOverlay(type = testCase.type, value = testCase.value)
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

        viewModel.session.test {
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

        viewModel.session.test {
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

        viewModel.session.test {
            awaitItem() shouldBe State.Error()
        }
    }

    test("error state when playerState is PlayerManager.State.Error") {
        playerManager = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(PlayerManager.State.Error())
        }
        updateVm()

        viewModel.session.test {
            awaitItem() shouldBe State.Error()
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

        viewModel.session.test {
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

        viewModel.session.test {
            awaitItem()
            val track = PlayerTrack(id = "1", label = "English", language = Language.ENGLISH.code, type = PlayerTrack.Type.AUDIO)

            // Should catch and not crash
            viewModel.handleIntent(PlayerIntent.SelectTrack(track))
        }
    }

})

private val PlayerViewModel<Player>.sessionContent: PlayerSession<Player>
    get() = (session.value as State.Content).content