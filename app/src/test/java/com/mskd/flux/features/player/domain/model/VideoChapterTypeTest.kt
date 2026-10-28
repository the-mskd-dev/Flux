package com.mskd.flux.features.player.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class VideoChapterTypeTest : FunSpec({

    context("fromString returns OPENING") {
        withData(
            "OP", "OP1", "Opening", "NCOP", "Intro", "Introduction", "opening", "OPENING",
            "Song Name OP", "Mixed OP", "オープニング", "ＯＰ"
        ) { title ->
            VideoChapterType.fromString(title) shouldBe VideoChapterType.OPENING
        }
    }

    context("fromString returns ENDING") {
        withData(
            "ED", "ED2", "Ending", "NCED", "Outro", "Credits", "Credit", "End Credits", "End", "ending",
            "Song Name ED", "Credits Start", "エンディング", "ＥＤ"
        ) { title ->
            VideoChapterType.fromString(title) shouldBe VideoChapterType.ENDING
        }
    }

    context("fromString returns CHAPTER") {
        withData(
            "Chapter 01", "01", "Opera", "Edward", "Prologue", "Avant", "Part A", "Part B",
            "Preview", "PV", "Recap",
            "OP End", "Opening End", "Ending: End", "ED End",
            "Post-Credits", "Post Credits", "Post Credit Scene", "Credits Scene",
            "Endgame", "Introspection", "Credit Union Heist", "Tired", "Shop",
            "Song Name OP End", "Credits Start End"
        ) { title ->
            VideoChapterType.fromString(title) shouldBe VideoChapterType.CHAPTER
        }

        test("empty title") {
            VideoChapterType.fromString("") shouldBe VideoChapterType.CHAPTER
        }

        test("null title") {
            VideoChapterType.fromString(null) shouldBe VideoChapterType.CHAPTER
        }
    }
})
