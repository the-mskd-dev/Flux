package com.mskd.flux.features.player.fake

import android.text.SpannedString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

data class SpanFixture(
    val span: CharacterStyle,
    val start: Int,
    val end: Int,
)

fun colorSpan(color: Int, start: Int, end: Int): SpanFixture = SpanFixture(
    span = mockk<ForegroundColorSpan> { every { foregroundColor } returns color },
    start = start,
    end = end,
)

fun styleSpan(style: Int, start: Int, end: Int): SpanFixture = SpanFixture(
    span = mockk<StyleSpan> { every { getStyle() } returns style },
    start = start,
    end = end,
)

fun underlineSpan(start: Int, end: Int): SpanFixture = SpanFixture(
    span = mockk<UnderlineSpan>(),
    start = start,
    end = end,
)

fun spannedText(text: String, vararg spans: SpanFixture): SpannedString {
    val spanned = mockk<SpannedString>()
    every { spanned.toString() } returns text
    every { spanned.length } returns text.length

    val characterSpans = spans.map { it.span }.toTypedArray()
    every { spanned.getSpans<CharacterStyle>(any(), any(), CharacterStyle::class.java) } returns characterSpans

    spans.forEach { fixture ->
        every { spanned.getSpanStart(fixture.span) } returns fixture.start
        every { spanned.getSpanEnd(fixture.span) } returns fixture.end
    }

    return spanned
}

fun FunSpec.stubSpannedStringValueOf() {
    beforeSpec {
        mockkStatic(SpannedString::class)
        every { SpannedString.valueOf(any()) } answers { firstArg<CharSequence>() as? SpannedString }
    }
    afterSpec {
        unmockkStatic(SpannedString::class)
    }
}
