package com.mskd.flux.features.player.data.mapper

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.media3.common.text.Cue
import com.mskd.flux.features.player.domain.model.CueAnchor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

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

    test("image cue maps bitmap zero-copy with normalized bounds and anchors") {
        val rawBitmap = mockk<Bitmap>(relaxed = true)
        val cue = Cue.Builder()
            .setBitmap(rawBitmap)
            .setSize(0.5f)
            .setBitmapHeight(0.25f)
            .setLine(0.8f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .setPosition(0.1f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_START)
            .build()

        val mapped = cue.toSubtitleCue()
        mapped.line shouldBe 0.8f
        mapped.lineAnchor shouldBe CueAnchor.START
        mapped.position shouldBe 0.1f
        mapped.positionAnchor shouldBe CueAnchor.START
        mapped.isStyled shouldBe true

        val subtitleBitmap = mapped.bitmap.shouldNotBeNull()
        subtitleBitmap.widthFraction shouldBe 0.5f
        subtitleBitmap.heightFraction shouldBe 0.25f
        subtitleBitmap.image.toImageBitmap().shouldNotBeNull()
        verify(exactly = 0) { rawBitmap.compress(any(), any(), any()) }
    }

    test("bitmap cue equality uses O(1) identity across consecutive frames") {
        val firstBitmap = mockk<Bitmap>(relaxed = true)
        val secondBitmap = mockk<Bitmap>(relaxed = true)
        fun bitmapCue(bitmap: Bitmap) = Cue.Builder()
            .setBitmap(bitmap)
            .setSize(0.5f)
            .setBitmapHeight(0.25f)
            .setLine(0.8f, Cue.LINE_TYPE_FRACTION)
            .setPosition(0.1f)
            .build()
            .toSubtitleCue()

        val cue1 = bitmapCue(firstBitmap)
        val cue2 = bitmapCue(secondBitmap)
        val sameCue1 = bitmapCue(firstBitmap)

        cue1 shouldNotBe cue2
        cue1.bitmap shouldNotBe cue2.bitmap
        cue1.bitmap.shouldNotBeNull().image shouldNotBe cue2.bitmap.shouldNotBeNull().image

        cue1 shouldBe sameCue1
        cue1.bitmap shouldBe sameCue1.bitmap
        cue1.bitmap.shouldNotBeNull().image.hashCode() shouldBe sameCue1.bitmap.shouldNotBeNull().image.hashCode()
    }

    test("image cue with invalid dimensions is dropped while 0f placement coordinates stay valid") {
        val rawBitmap = mockk<Bitmap>(relaxed = true)
        val invalidValues = listOf(Cue.DIMEN_UNSET, 0f, -0.1f, 1.1f, Float.NaN)

        invalidValues.forEach { invalidWidth ->
            val cue = Cue.Builder()
                .setBitmap(rawBitmap)
                .setSize(invalidWidth)
                .setBitmapHeight(0.25f)
                .build()
            cue.toSubtitleCue().bitmap shouldBe null
        }

        invalidValues.forEach { invalidHeight ->
            val cue = Cue.Builder()
                .setBitmap(rawBitmap)
                .setSize(0.5f)
                .setBitmapHeight(invalidHeight)
                .build()
            cue.toSubtitleCue().bitmap shouldBe null
        }

        val edgeCue = Cue.Builder()
            .setBitmap(rawBitmap)
            .setSize(0.5f)
            .setBitmapHeight(0.25f)
            .setLine(0f, Cue.LINE_TYPE_FRACTION)
            .setPosition(0f)
            .build()
            .toSubtitleCue()

        edgeCue.line shouldBe 0f
        edgeCue.position shouldBe 0f
        edgeCue.bitmap.shouldNotBeNull()
    }
})
