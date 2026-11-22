package com.mskd.flux.features.player.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
                    )
                }
            }
        }
    }

    private fun boundsOf(tag: String): DpRect = composeRule.onNodeWithTag(tag).getBoundsInRoot()

    @Test
    fun cuesSitAtTheirAuthoredCoordinatesInsideTheFrame() {
        setSubtitles(listOf(bitmapCue, positionedCue, plainCue))

        // Centred sign covering 20% x 10% of the 400 x 300 frame
        val bitmap = boundsOf(PlayerSubtitlesTags.BITMAP_CUE)
        assertThat(bitmap.width.value).isWithin(1f).of(80f)
        assertThat(bitmap.height.value).isWithin(1f).of(30f)
        assertThat(bitmap.centerX).isWithin(1f).of(200f)
        assertThat(bitmap.centerY).isWithin(1f).of(150f)

        // 10% from the top, centred
        val positioned = boundsOf(PlayerSubtitlesTags.POSITIONED_TEXT_CUE)
        assertThat(positioned.top.value).isWithin(1f).of(30f)
        assertThat(positioned.centerX).isWithin(1f).of(200f)

        assertThat(boundsOf(PlayerSubtitlesTags.BOTTOM_TEXT_CUE).bottom.value).isAtMost(300f)
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
