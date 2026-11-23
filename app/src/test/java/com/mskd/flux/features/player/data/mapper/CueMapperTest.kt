package com.mskd.flux.features.player.data.mapper

import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.style.CharacterStyle
import androidx.media3.common.text.Cue
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.SubtitleTextSpan
import com.mskd.flux.features.player.fake.SpanFixture
import com.mskd.flux.features.player.fake.colorSpan
import com.mskd.flux.features.player.fake.spannedText
import com.mskd.flux.features.player.fake.stubSpannedStringValueOf
import com.mskd.flux.features.player.fake.styleSpan
import com.mskd.flux.features.player.fake.underlineSpan
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import io.mockk.verify

class CueMapperTest : FunSpec({

    stubSpannedStringValueOf()

    test("plain cue without style or position keeps default placement") {
        val cue = Cue.Builder().setText("Hello").build()

        cue.toSubtitleCue().let {
            it.text shouldBe "Hello"
            it.spans shouldBe emptyList()
            it.line shouldBe null
            it.position shouldBe null
            it.isPositioned shouldBe false
        }
    }

    test("per-span bold, italic, bold-italic, underline, and color extract exact ranges without positioning") {
        // "Hello world, watch carefully!" -> bold "world" (6..11), italic "watch" (13..18),
        // bold-italic "!" (28..29), underline "carefully" (19..28), color "Hello" (0..5),
        // and Typeface.NORMAL span on "watch" is ignored.
        val text = "Hello world, watch carefully!"
        val spanned = spannedText(
            text = text,
            colorSpan(color = 0xFFFF0000.toInt(), start = 0, end = 5),
            styleSpan(style = Typeface.BOLD, start = 6, end = 11),
            styleSpan(style = Typeface.ITALIC, start = 13, end = 18),
            styleSpan(style = Typeface.BOLD_ITALIC, start = 28, end = 29),
            styleSpan(style = Typeface.NORMAL, start = 13, end = 18),
            underlineSpan(start = 19, end = 28),
        )

        val mapped = Cue.Builder().setText(spanned).build().toSubtitleCue()

        mapped.text shouldBe text
        mapped.isPositioned shouldBe false
        mapped.line shouldBe null
        mapped.position shouldBe null
        mapped.spans shouldContainExactly listOf(
            SubtitleTextSpan(start = 0, end = 5, color = 0xFFFF0000.toInt()),
            SubtitleTextSpan(start = 6, end = 11, bold = true),
            SubtitleTextSpan(start = 13, end = 18, italic = true),
            SubtitleTextSpan(start = 28, end = 29, bold = true, italic = true),
            SubtitleTextSpan(start = 19, end = 28, underline = true),
        )
    }

    test("overlapping and nested spans preserve individual ranges and order") {
        val text = "Nested red blue"
        val spanned = spannedText(
            text = text,
            colorSpan(color = 0xFFFF0000.toInt(), start = 0, end = 15),
            colorSpan(color = 0xFF0000FF.toInt(), start = 11, end = 15),
            styleSpan(style = Typeface.ITALIC, start = 7, end = 15),
            underlineSpan(start = 0, end = 6),
        )

        val mapped = Cue.Builder().setText(spanned).build().toSubtitleCue()

        mapped.isPositioned shouldBe false
        mapped.spans shouldContainExactly listOf(
            SubtitleTextSpan(start = 0, end = 15, color = 0xFFFF0000.toInt()),
            SubtitleTextSpan(start = 11, end = 15, color = 0xFF0000FF.toInt()),
            SubtitleTextSpan(start = 7, end = 15, italic = true),
            SubtitleTextSpan(start = 0, end = 6, underline = true),
        )
    }

    test("explicitly positioned text cue keeps coordinates, anchors, and inline spans") {
        val spanned = spannedText(
            text = "Translated sign",
            styleSpan(style = Typeface.BOLD, start = 0, end = 10),
        )
        val cue = Cue.Builder()
            .setText(spanned)
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
            it.isPositioned shouldBe true
            it.spans shouldContainExactly listOf(
                SubtitleTextSpan(start = 0, end = 10, bold = true)
            )
        }
    }

    test("out-of-bounds spans are clamped to text length") {
        val text = "0123456789" // length 10
        val spanned = spannedText(
            text = text,
            styleSpan(style = Typeface.BOLD, start = -3, end = 4),
            styleSpan(style = Typeface.ITALIC, start = 6, end = 50),
            colorSpan(color = 0xFF00FF00.toInt(), start = -5, end = 99),
        )

        val mapped = Cue.Builder().setText(spanned).build().toSubtitleCue()

        mapped.spans shouldContainExactly listOf(
            SubtitleTextSpan(start = 0, end = 4, bold = true),
            SubtitleTextSpan(start = 6, end = 10, italic = true),
            SubtitleTextSpan(start = 0, end = 10, color = 0xFF00FF00.toInt()),
        )
    }

    test("degenerate spans and empty text produce no spans") {
        val text = "0123456789" // length 10
        val spanned = spannedText(
            text = text,
            styleSpan(style = Typeface.BOLD, start = -1, end = -1), // detached
            styleSpan(style = Typeface.ITALIC, start = 4, end = 4), // zero-length
            underlineSpan(start = 8, end = 3), // inverted
            colorSpan(color = 0xFFFF0000.toInt(), start = 12, end = 20), // completely past end
        )

        Cue.Builder().setText(spanned).build().toSubtitleCue().spans shouldBe emptyList()

        val emptySpanned = spannedText(
            text = "",
            styleSpan(style = Typeface.BOLD, start = 0, end = 5),
        )
        Cue.Builder().setText(emptySpanned).build().toSubtitleCue().spans shouldBe emptyList()
    }

    test("unpositioned WebVTT cue with default centered position routes to bottom band") {
        val spanned = spannedText(
            text = "WebVTT line",
            styleSpan(style = Typeface.ITALIC, start = 0, end = 6),
        )
        val cue = Cue.Builder()
            .setText(spanned)
            .setLine(-1f, Cue.LINE_TYPE_NUMBER)
            .setPosition(0.5f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_MIDDLE)
            .build()

        cue.toSubtitleCue().let {
            it.line shouldBe null
            it.position shouldBe null
            it.isPositioned shouldBe false
            it.spans shouldContainExactly listOf(SubtitleTextSpan(start = 0, end = 6, italic = true))
        }
    }

    test("horizontally positioned WebVTT cues stay explicitly positioned") {
        val shiftedCue = Cue.Builder()
            .setText("Left cue")
            .setLine(-1f, Cue.LINE_TYPE_NUMBER)
            .setPosition(0.2f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_MIDDLE)
            .build()

        shiftedCue.toSubtitleCue().let {
            it.line shouldBe null
            it.position shouldBe 0.2f
            it.positionAnchor shouldBe CueAnchor.CENTER
            it.isPositioned shouldBe true
        }

        val anchoredAtCenterCue = Cue.Builder()
            .setText("Start-anchored at 50%")
            .setLine(-1f, Cue.LINE_TYPE_NUMBER)
            .setPosition(0.5f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_START)
            .build()

        anchoredAtCenterCue.toSubtitleCue().let {
            it.line shouldBe null
            it.position shouldBe 0.5f
            it.positionAnchor shouldBe CueAnchor.START
            it.isPositioned shouldBe true
        }
    }

    test("vertically positioned WebVTT cue preserves default centered horizontal coordinate") {
        val cue = Cue.Builder()
            .setText("Top center note")
            .setLine(0.1f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .setPosition(0.5f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_MIDDLE)
            .build()

        cue.toSubtitleCue().let {
            it.line shouldBe 0.1f
            it.position shouldBe 0.5f
            it.positionAnchor shouldBe CueAnchor.CENTER
            it.isPositioned shouldBe true
        }
    }

    test("unsupported CharacterStyle spans are ignored cleanly") {
        val text = "Plain with unknown span"
        val spanned = spannedText(
            text = text,
            SpanFixture(mockk<CharacterStyle>(), start = 0, end = 5),
        )

        Cue.Builder().setText(spanned).build().toSubtitleCue().spans shouldBe emptyList()
    }

    test("a line counted in lines, not as a fraction of the frame, is not placed") {
        val cue = Cue.Builder().setText("Numbered").setLine(3f, Cue.LINE_TYPE_NUMBER).build()

        cue.toSubtitleCue().let {
            it.line shouldBe null
            it.isPositioned shouldBe false
        }
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
        mapped.isPositioned shouldBe true

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
