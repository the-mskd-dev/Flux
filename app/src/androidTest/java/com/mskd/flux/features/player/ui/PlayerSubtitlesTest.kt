package com.mskd.flux.features.player.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleBitmap
import com.mskd.flux.features.player.domain.model.SubtitleCue
import com.mskd.flux.features.player.domain.model.SubtitleImage
import com.mskd.flux.features.player.domain.model.SubtitleTextSpan
import com.mskd.flux.ui.theme.FluxTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerSubtitlesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val zoom = mutableFloatStateOf(1f)

    /** A centred sign covering 20% x 10% of the frame. */
    private val bitmapCue = SubtitleCue(
        line = 0.5f,
        lineAnchor = CueAnchor.CENTER,
        position = 0.5f,
        positionAnchor = CueAnchor.CENTER,
        bitmap = SubtitleBitmap(
            image = SubtitleImage(Bitmap.createBitmap(80, 30, Bitmap.Config.ARGB_8888)),
            widthFraction = 0.2f,
            heightFraction = 0.1f,
        ),
    )

    private val positionedCue = SubtitleCue(
        text = "Top sign translation",
        line = 0.1f,
        lineAnchor = CueAnchor.START,
        position = 0.5f,
    )

    private val plainCue = SubtitleCue(text = "Plain dialogue line")

    private val styledCue = SubtitleCue(
        text = "Styled dialogue line",
        spans = listOf(SubtitleTextSpan(start = 0, end = 6, italic = true, color = 0xFFFFFF00.toInt())),
    )

    private fun setSubtitles(cues: List<SubtitleCue>) {
        composeRule.setContent {
            FluxTheme {
                Box(modifier = Modifier.size(width = 400.dp, height = 300.dp)) {
                    PlayerSubtitles(
                        cues = { cues },
                        smallText = false,
                        bitmapScale = { zoom.floatValue },
                    )
                }
            }
        }
    }

    private fun boundsOf(tag: String): DpRect = composeRule.onNodeWithTag(tag).getBoundsInRoot()

    private fun zoomTo(scale: Float) {
        zoom.floatValue = scale
        composeRule.waitForIdle()
    }

    @Test
    fun bitmapLayerScalesWithZoomAroundTheFrameCentre() {
        setSubtitles(listOf(bitmapCue))

        val unzoomed = boundsOf(PlayerSubtitlesTags.BITMAP_CUE)
        assertThat(unzoomed.width.value).isWithin(1f).of(80f)
        assertThat(unzoomed.height.value).isWithin(1f).of(30f)

        zoomTo(2f)
        val zoomed = boundsOf(PlayerSubtitlesTags.BITMAP_CUE)

        assertThat(zoomed.width.value).isWithin(1f).of(160f)
        assertThat(zoomed.height.value).isWithin(1f).of(60f)
        assertThat(zoomed.centerX).isWithin(1f).of(unzoomed.centerX)
        assertThat(zoomed.centerY).isWithin(1f).of(unzoomed.centerY)
    }

    @Test
    fun textLayersStayUnzoomedWhileBitmapsZoom() {
        setSubtitles(listOf(bitmapCue, positionedCue, plainCue))

        val positioned = boundsOf(PlayerSubtitlesTags.POSITIONED_TEXT_CUE)
        val bottom = boundsOf(PlayerSubtitlesTags.BOTTOM_TEXT_CUE)

        zoomTo(1.5f)

        assertThat(boundsOf(PlayerSubtitlesTags.POSITIONED_TEXT_CUE)).isEqualTo(positioned)
        assertThat(boundsOf(PlayerSubtitlesTags.BOTTOM_TEXT_CUE)).isEqualTo(bottom)
        assertThat(boundsOf(PlayerSubtitlesTags.BITMAP_CUE).width.value).isWithin(1f).of(120f)

        // Authored coordinates: 10% from the top, centred, all inside the frame
        assertThat(positioned.top.value).isWithin(1f).of(30f)
        assertThat(positioned.centerX).isWithin(1f).of(200f)
        assertThat(bottom.bottom.value).isAtMost(300f)
    }

    @Test
    fun simultaneousPlainAndStyledCuesStackWithoutOverlapping() {
        setSubtitles(listOf(plainCue, styledCue))

        val nodes = composeRule.onAllNodesWithTag(PlayerSubtitlesTags.BOTTOM_TEXT_CUE).fetchSemanticsNodes()
        assertThat(nodes).hasSize(2)

        val stacked = nodes.map { it.boundsInRoot }.sortedBy { it.top }
        assertThat(stacked[0].bottom).isAtMost(stacked[1].top)
        assertThat(stacked[0].height).isGreaterThan(0f)
        assertThat(stacked[1].height).isGreaterThan(0f)
    }

    private val DpRect.centerX: Float get() = (left.value + right.value) / 2f
    private val DpRect.centerY: Float get() = (top.value + bottom.value) / 2f
}
