package com.mskd.flux.features.files.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ExternalSubtitleMatcherTest : FunSpec({

    test("matches exact base name across all supported formats case-insensitively") {
        val candidates = listOf(
            SubtitleCandidate("uri://srt", "EPISODE01.SRT"),
            SubtitleCandidate("uri://vtt", "episode01.vtt"),
            SubtitleCandidate("uri://ssa", "Episode01.SSA"),
            SubtitleCandidate("uri://ass", "Episode01.ass"),
        )

        val matched = ExternalSubtitleMatcher.matchAll("Episode01.mkv", candidates)

        matched.map { it.uri } shouldContainExactly listOf(
            "uri://ass",
            "uri://ssa",
            "uri://srt",
            "uri://vtt",
        )
        matched.forEach {
            it.language.shouldBeNull()
            it.tags.shouldBeEmpty()
        }
    }

    test("rejects non-dot prefix collisions, malformed dot tokens, and unsupported extensions") {
        val candidates = listOf(
            SubtitleCandidate("uri://prefix-digit", "Episode010.ass"),
            SubtitleCandidate("uri://prefix-word", "Episode01Extended.srt"),
            SubtitleCandidate("uri://underscore", "Episode01_fr.ass"),
            SubtitleCandidate("uri://dash", "Episode01-fr.ass"),
            SubtitleCandidate("uri://space", "Episode01 .ass"),
            SubtitleCandidate("uri://double-dot", "Episode01..ass"),
            SubtitleCandidate("uri://trailing-empty-token", "Episode01.fr..ass"),
            SubtitleCandidate("uri://blank-token", "Episode01. .ass"),
            SubtitleCandidate("uri://hidden-file", ".ass"),
            SubtitleCandidate("uri://unsupported-txt", "Episode01.fr.txt"),
            SubtitleCandidate("uri://unsupported-mkv", "Episode01.mkv"),
            SubtitleCandidate("uri://sup", "Episode01.sup"),
        )

        ExternalSubtitleMatcher.matchAll("Episode01.mkv", candidates).shouldBeEmpty()
    }

    test("handles UserFile names containing dots without prefix collisions") {
        val matched = ExternalSubtitleMatcher.matchAll(
            userFileName = "My.Show.S01E01.mkv",
            candidates = listOf(
                SubtitleCandidate("uri://good", "My.Show.S01E01.fr.forced.ass"),
                SubtitleCandidate("uri://bad", "My.Show.S01E010.fr.ass"),
            )
        )

        matched shouldContainExactly listOf(
            ExternalSubtitle(
                uri = "uri://good",
                fileName = "My.Show.S01E01.fr.forced.ass",
                extension = "ass",
                language = "fr",
                tags = listOf("forced"),
            )
        )
    }

    test("extracts ISO 639-1, ISO 639-2 mapped to ISO 639-1, and regional or script language tags") {
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.fr.ass", "u1")?.language shouldBe "fr"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.FRE.ass", "u2")?.language shouldBe "fr"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.fra.ass", "u3")?.language shouldBe "fr"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.ger.ass", "u4")?.language shouldBe "de"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.deu.ass", "u5")?.language shouldBe "de"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.jpn.srt", "u6")?.language shouldBe "ja"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.eng.vtt", "u7")?.language shouldBe "en"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.pt-br.ass", "u8")?.language shouldBe "pt-BR"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.pt_BR.ass", "u9")?.language shouldBe "pt-BR"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.por-br.ass", "u10")?.language shouldBe "pt-BR"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.zh-hans.ass", "u11")?.language shouldBe "zh-Hans"
        ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.es-419.srt", "u12")?.language shouldBe "es-419"
    }

    test("preserves descriptor tokens without misclassifying them as languages") {
        val forced = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.fr.forced.ass", "u1").shouldNotBeNull()
        forced.language shouldBe "fr"
        forced.tags shouldBe listOf("forced")

        val multiTag = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.en.signs.sdh.cc.srt", "u2").shouldNotBeNull()
        multiTag.language shouldBe "en"
        multiTag.tags shouldBe listOf("signs", "sdh", "cc")

        val tagBeforeLang = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.forced.fr.ass", "u3").shouldNotBeNull()
        tagBeforeLang.language shouldBe "fr"
        tagBeforeLang.tags shouldBe listOf("forced")

        val untaggedWithDescriptors = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.signs.forced.ass", "u4").shouldNotBeNull()
        untaggedWithDescriptors.language.shouldBeNull()
        untaggedWithDescriptors.tags shouldBe listOf("signs", "forced")

        val hearingImpaired = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.en.hi.srt", "u5").shouldNotBeNull()
        hearingImpaired.language shouldBe "en"
        hearingImpaired.tags shouldBe listOf("hi")
    }

    test("orders ASS/SSA before SRT/VTT, non-forced before forced, then filename") {
        val candidates = listOf(
            SubtitleCandidate("uri://en-srt", "Episode01.en.srt"),
            SubtitleCandidate("uri://fr-forced-ass", "Episode01.fr.forced.ass"),
            SubtitleCandidate("uri://en-vtt", "Episode01.en.vtt"),
            SubtitleCandidate("uri://fr-srt", "Episode01.fr.srt"),
            SubtitleCandidate("uri://ja-ssa", "Episode01.ja.ssa"),
            SubtitleCandidate("uri://fr-ass", "Episode01.fr.ass"),
            SubtitleCandidate("uri://en-ass", "Episode01.en.ass"),
            SubtitleCandidate("uri://untagged-ass", "Episode01.ass"),
        )

        val ordered = ExternalSubtitleMatcher.matchAll("Episode01.mkv", candidates)

        ordered.map { it.fileName } shouldContainExactly listOf(
            "Episode01.ass",
            "Episode01.en.ass",
            "Episode01.fr.ass",
            "Episode01.ja.ssa",
            "Episode01.fr.forced.ass",
            "Episode01.en.srt",
            "Episode01.en.vtt",
            "Episode01.fr.srt",
        )
    }

    test("formats Track labels with language display name or external fallback, descriptor tags, and format") {
        val displayNames = mapOf("fr" to "Français", "en" to "English")

        val tagged = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.fr.ass", "u1").shouldNotBeNull()
        tagged.formatLabel(externalFallback = "External") { displayNames[it] ?: it } shouldBe "Français (ASS)"

        val taggedForced = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.fr.forced.ass", "u2").shouldNotBeNull()
        taggedForced.formatLabel(externalFallback = "External") { displayNames[it] ?: it } shouldBe "Français · forced (ASS)"

        val taggedMulti = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.en.signs.sdh.srt", "u3").shouldNotBeNull()
        taggedMulti.formatLabel(externalFallback = "External") { displayNames[it] ?: it } shouldBe "English · signs · sdh (SRT)"

        val untagged = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.ass", "u4").shouldNotBeNull()
        untagged.formatLabel(externalFallback = "External") { displayNames[it] ?: it } shouldBe "External (ASS)"

        val untaggedLocalized = ExternalSubtitleMatcher.match("Ep01.mkv", "Ep01.signs.vtt", "u5").shouldNotBeNull()
        untaggedLocalized.formatLabel(externalFallback = "Externe") { displayNames[it] ?: it } shouldBe "Externe · signs (VTT)"
    }
})
