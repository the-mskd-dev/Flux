package com.mskd.flux.features.player.domain.selection

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SubtitleSelectionPolicyTest : FunSpec({

    fun embedded(
        name: String,
        language: String?,
        format: SubtitleTrackFormat = SubtitleTrackFormat.OTHER,
        isForced: Boolean = false,
    ) = SubtitleTrackDescription(
        track = name,
        language = language,
        externalOrder = null,
        format = format,
        isForced = isForced,
    )

    fun external(
        name: String,
        order: Int,
        language: String?,
        format: SubtitleTrackFormat = SubtitleTrackFormat.OTHER,
        isForced: Boolean = false,
    ) = SubtitleTrackDescription(
        track = name,
        language = language,
        externalOrder = order,
        format = format,
        isForced = isForced,
    )

    test("an external Track is selected ahead of an embedded Track of the preferred language") {
        val tracks = listOf(
            embedded("embedded fr ASS", language = "fr", format = SubtitleTrackFormat.ASS_SSA),
            external("external fr SRT", order = 0, language = "fr"),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.Override("external fr SRT")
    }

    test("an external Track matching only the primary language beats an embedded exact match") {
        val tracks = listOf(
            embedded("embedded pt-BR", language = "pt-BR"),
            external("external pt", order = 0, language = "pt"),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "pt-BR", tracks = tracks) shouldBe
            SubtitleSelection.Override("external pt")
    }

    test("an exact regional match beats a primary-only match among external Tracks, whatever the format") {
        val tracks = listOf(
            external("external pt-PT ASS", order = 0, language = "pt-pt", format = SubtitleTrackFormat.ASS_SSA),
            external("external pt-BR SRT", order = 1, language = "pt-br"),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "pt-BR", tracks = tracks) shouldBe
            SubtitleSelection.Override("external pt-BR SRT")
    }

    test("a regional Track matches the primary preferred language") {
        val tracks = listOf(external("external pt-BR", order = 0, language = "pt-br"))

        SubtitleSelectionPolicy.select(preferredLanguage = "pt", tracks = tracks) shouldBe
            SubtitleSelection.Override("external pt-BR")
    }

    test("an external ASS/SSA Track beats an external SRT/VTT Track of the same language") {
        val tracks = listOf(
            external("external fr SRT", order = 0, language = "fr"),
            external("external fr ASS", order = 1, language = "fr", format = SubtitleTrackFormat.ASS_SSA),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.Override("external fr ASS")
    }

    test("a non-forced Track beats a forced Track of the same language and format") {
        val tracks = listOf(
            external("external fr forced", order = 0, language = "fr", isForced = true),
            external("external fr", order = 1, language = "fr"),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.Override("external fr")
    }

    test("external order breaks ties among otherwise equal external Tracks, regardless of list position") {
        val tracks = listOf(
            external("external fr #2", order = 2, language = "fr"),
            external("external fr #1", order = 1, language = "fr"),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.Override("external fr #1")
    }

    test("language matching ignores case") {
        val tracks = listOf(external("external FR", order = 0, language = "FR"))

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.Override("external FR")
    }

    test("with only embedded Tracks matching the preferred language, no Track is forced whatever their format, so the container flags decide") {
        val tracks = listOf(
            embedded("embedded fr PGS", language = "fr"),
            embedded("embedded fr ASS signs", language = "fr", format = SubtitleTrackFormat.ASS_SSA),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.PreferredLanguageOnly
    }

    test("an embedded Track matching the preferred language prevents the untagged external fallback") {
        val tracks = listOf(
            embedded("embedded fr", language = "fr"),
            external("external untagged", order = 0, language = null, format = SubtitleTrackFormat.ASS_SSA),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.PreferredLanguageOnly
    }

    test("with no Track matching the preferred language, the untagged external Track is selected by format") {
        val tracks = listOf(
            embedded("embedded ja", language = "ja"),
            external("external en", order = 0, language = "en", format = SubtitleTrackFormat.ASS_SSA),
            external("external untagged SRT", order = 1, language = null),
            external("external untagged ASS", order = 2, language = "", format = SubtitleTrackFormat.ASS_SSA),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.Override("external untagged ASS")
    }

    test("with no preferred language, the untagged external Track is selected") {
        val tracks = listOf(
            embedded("embedded fr", language = "fr"),
            external("external untagged", order = 0, language = null),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = null, tracks = tracks) shouldBe
            SubtitleSelection.Override("external untagged")
    }

    test("with no matching Track and no untagged external Track, only the preferred language is set") {
        val tracks = listOf(
            embedded("embedded untagged", language = null),
            embedded("embedded ja", language = "ja"),
            external("external en", order = 0, language = "en"),
        )

        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = tracks) shouldBe
            SubtitleSelection.PreferredLanguageOnly
    }

    test("with no Track at all, only the preferred language is set") {
        SubtitleSelectionPolicy.select(preferredLanguage = "fr", tracks = emptyList<SubtitleTrackDescription<String>>()) shouldBe
            SubtitleSelection.PreferredLanguageOnly
    }

    test("external subtitle IDs round-trip their external order") {
        ExternalSubtitleTrackId.externalOrderOf(ExternalSubtitleTrackId.of(4)) shouldBe 4
    }

    test("external subtitle IDs are recognized behind MergingMediaSource prefixes") {
        ExternalSubtitleTrackId.externalOrderOf("flux-ext-0") shouldBe 0
        ExternalSubtitleTrackId.externalOrderOf("2:flux-ext-1") shouldBe 1
        ExternalSubtitleTrackId.externalOrderOf("0:1:flux-ext-3") shouldBe 3
    }

    test("embedded and malformed IDs carry no external order") {
        listOf(null, "emb-1", "0:0:3", "flux-ext-", "flux-ext-a", "a:flux-ext-1", "flux-ext-1:0").forEach {
            ExternalSubtitleTrackId.externalOrderOf(it) shouldBe null
        }
    }
})
