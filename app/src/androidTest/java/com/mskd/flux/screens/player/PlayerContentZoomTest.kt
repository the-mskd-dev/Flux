package com.mskd.flux.screens.player

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.height
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleBitmap
import com.mskd.flux.features.player.domain.model.SubtitleCue
import com.mskd.flux.features.player.domain.model.SubtitleImage
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerOverlays
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.features.player.presentation.rememberWindowStateHolder
import com.mskd.flux.features.player.ui.PlayerSubtitlesTags
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.theme.FluxTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Pinch-to-fill zooms the picture only: subtitles keep their size and position. */
@OptIn(UnstableApi::class)
@RunWith(AndroidJUnit4::class)
class PlayerContentZoomTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** A dialogue line rasterized by libass: bottom centred, 60% x 10% of the frame. */
    private val dialogueBitmapCue = SubtitleCue(
        line = 0.95f,
        lineAnchor = CueAnchor.END,
        position = 0.5f,
        positionAnchor = CueAnchor.CENTER,
        bitmap = SubtitleBitmap(
            image = SubtitleImage(Bitmap.createBitmap(600, 60, Bitmap.Config.ARGB_8888)),
            widthFraction = 0.6f,
            heightFraction = 0.1f,
        ),
    )

    private val plainCue = SubtitleCue(text = "Plain dialogue line")

    private fun setPlayer(cues: List<SubtitleCue>) {
        composeRule.setContent {
            val context = LocalContext.current
            // Cinemascope picture: pinch-to-fill zooms it to cover the screen
            val player = remember {
                object : ForwardingPlayer(ExoPlayer.Builder(context).build()) {
                    override fun getVideoSize() = VideoSize(1920, 800)
                }
            }
            DisposableEffect(player) { onDispose { player.release() } }

            val landscape = Configuration(LocalConfiguration.current).apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
            }

            CompositionLocalProvider(LocalConfiguration provides landscape) {
                FluxTheme {
                    PlayerContent(
                        session = PlayerSession(media = MediaMockups.movie, player = player),
                        interfaceState = MutableStateFlow(PlayerInterfaceState()),
                        overlays = MutableStateFlow(PlayerOverlays()),
                        isPlaying = MutableStateFlow(false),
                        volume = emptyFlow(),
                        windowStateHolder = rememberWindowStateHolder(),
                        subtitles = { cues },
                        progress = { 0L },
                        focusRequester = remember { FocusRequester() },
                        sendIntent = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun boundsOf(tag: String): DpRect = composeRule.onNodeWithTag(tag).getBoundsInRoot()

    /** Pinches open and checks the picture did zoom, so a fixed cue proves something. */
    private fun pinchOpenToFill() {
        val picture = boundsOf(PlayerScreenTags.PICTURE)

        composeRule.onRoot().performTouchInput {
            pinch(
                start0 = center - Offset(20f, 0f),
                end0 = center - Offset(400f, 0f),
                start1 = center + Offset(20f, 0f),
                end1 = center + Offset(400f, 0f),
            )
        }
        composeRule.waitForIdle()

        val zoomed = boundsOf(PlayerScreenTags.PICTURE)
        assertThat(zoomed.height.value).isGreaterThan(picture.height.value * 1.5f)
    }

    @Test
    fun bitmapSubtitlesKeepTheirSizeAndPositionWhenPinchingToFill() {
        setPlayer(listOf(dialogueBitmapCue))
        val before = boundsOf(PlayerSubtitlesTags.BITMAP_CUE)

        pinchOpenToFill()

        assertThat(boundsOf(PlayerSubtitlesTags.BITMAP_CUE)).isEqualTo(before)
    }

    @Test
    fun textSubtitlesKeepTheirSizeAndPositionWhenPinchingToFill() {
        setPlayer(listOf(plainCue))
        val before = boundsOf(PlayerSubtitlesTags.BOTTOM_TEXT_CUE)

        pinchOpenToFill()

        assertThat(boundsOf(PlayerSubtitlesTags.BOTTOM_TEXT_CUE)).isEqualTo(before)
    }
}
