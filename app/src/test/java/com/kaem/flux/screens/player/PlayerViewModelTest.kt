package com.kaem.flux.screens.player

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.artwork.ArtworkRepository
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.FakeArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest : BaseTest() {

    private lateinit var viewModel: PlayerViewModel

    private lateinit var artworkRepository: FakeArtworkRepository

    private lateinit var userRepository: UserRepository

    private lateinit var settingsRepository: SettingsRepository

    override fun setUp() {
        super.setUp()

        artworkRepository = FakeArtworkRepository(initialContentType = ContentType.SHOW)

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserPreferences())
        }

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsPreferences())
        }

        viewModel = PlayerViewModel(
            mediaId = MediaMockups.episode1.mediaId,
            artworkRepository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository
        )

    }

    @Test
    fun show_interface() = runTest {

        viewModel.uiState.test { viewModel

            // Hidden by default
            val initialState = awaitItem()
            assert(!initialState.controls.showInterface)

            // Test show
            viewModel.handleIntent(PlayerIntent.ShowInterface)
            val showedState = awaitItem()
            assert(showedState.controls.showInterface)

            // Test hide
            viewModel.handleIntent(PlayerIntent.ShowInterface)
            val hiddenState = awaitItem()
            assert(!hiddenState.controls.showInterface)

        }

    }

    @Test
    fun show_settings() = runTest {
        //TODO
    }

    @Test
    fun save_time() = runTest {
        //TODO
    }

    @Test
    fun back_tap() = runTest {
        //TODO
    }

    @Test
    fun toggle_play_button() = runTest {
        //TODO
    }

    @Test
    fun set_playing_status() = runTest {
        //TODO
    }

    @Test
    fun fast_rewind() = runTest {
        //TODO
    }

    @Test
    fun fast_forward() = runTest {
        //TODO
    }

    @Test
    fun update_progress() = runTest {
        //TODO
    }

    @Test
    fun update_tracks() = runTest {
        //TODO
    }

    @Test
    fun select_track() = runTest {
        //TODO
    }

    @Test
    fun on_track_selected() = runTest {
        //TODO
    }

    @Test
    fun show_next_episode() = runTest {
        //TODO
    }

    @Test
    fun cancel_next_episode() = runTest {
        //TODO
    }

    @Test
    fun play_next_episode() = runTest {
        //TODO
    }

}