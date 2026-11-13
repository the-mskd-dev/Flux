package com.mskd.flux.features.player.data.mapper

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.media3.common.text.Cue
import com.mskd.flux.features.player.domain.model.CueAnchor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk

class CueMapperTest : FunSpec({

    // Spans are Android objects and Media3 copies a Spanned when a cue is built, so the style
    // is tested on the text itself: the text is a mock answering its spans explicitly.
    fun spannedOf(
        text: String,
        color: Int? = null,
        style: Int? = null,
    ): Spanned {
        val spanned = mockk<Spanned>()
        every { spanned.toString() } returns text
        every { spanned.length } returns text.length
        val colorSpans: Array<ForegroundColorSpan> =
            color?.let { arrayOf(mockk<ForegroundColorSpan> { every { foregroundColor } returns it }) }
                ?: emptyArray()
        val styleSpans: Array<StyleSpan> =
            style?.let { arrayOf(mockk<StyleSpan> { every { getStyle() } returns it }) }
                ?: emptyArray()
        every { spanned.getSpans<ForegroundColorSpan>(any(), any(), ForegroundColorSpan::class.java) } returns colorSpans
        every { spanned.getSpans<StyleSpan>(any(), any(), StyleSpan::class.java) } returns styleSpans
        return spanned
    }

    test("plain cue without style or position keeps the default look") {
        val cue = Cue.Builder().setText("Hello").build()

        cue.toSubtitleCue().let {
            it.text shouldBe "Hello"
            it.isStyled shouldBe false
        }
    }

    test("color and bold are read from the spans of a styled text") {
        val style = spannedOf(text = "Translated sign", color = 0xFFFF0000.toInt(), style = Typeface.BOLD).textStyle()

        style shouldBe CueTextStyle(color = 0xFFFF0000.toInt(), bold = true, italic = false)
    }

    test("italic style is mapped to italic, not bold") {
        val style = spannedOf(text = "Thought", style = Typeface.ITALIC).textStyle()

        style shouldBe CueTextStyle(color = null, bold = false, italic = true)
    }

    test("a styled cue keeps its placement from the file, with anchors") {
        val cue = Cue.Builder()
            .setText("Translated sign")
            .setLine(0.05f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .setPosition(0.5f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_MIDDLE)
            .build()

        cue.toSubtitleCue().let {
            it.line shouldBe 0.05f
            it.lineAnchor shouldBe CueAnchor.START
            it.position shouldBe 0.5f
            it.positionAnchor shouldBe CueAnchor.CENTER
            it.isStyled shouldBe true
        }
    }

    test("a line counted in lines, not as a fraction of the frame, is not placed") {
        val cue = Cue.Builder().setText("Numbered").setLine(3f, Cue.LINE_TYPE_NUMBER).build()

        cue.toSubtitleCue().line shouldBe null
    }

    test("image cue keeps its size as a fraction of the frame") {
        val cue = Cue.Builder()
            .setBitmap(mockk<Bitmap>(relaxed = true))
            .setSize(0.5f)
            .setBitmapHeight(0.25f)
            .setLine(0.8f, Cue.LINE_TYPE_FRACTION)
            .setPosition(0.1f)
            .build()

        cue.toSubtitleCue().bitmap.shouldNotBeNull().let {
            it.widthFraction shouldBe 0.5f
            it.heightFraction shouldBe 0.25f
        }
    }

    test("image cue without a known size is dropped rather than drawn at a guessed size") {
        val cue = Cue.Builder().setBitmap(mockk<Bitmap>(relaxed = true)).build()

        cue.toSubtitleCue().bitmap shouldBe null
    }
})
