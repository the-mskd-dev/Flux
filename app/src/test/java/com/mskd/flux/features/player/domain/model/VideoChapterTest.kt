package com.mskd.flux.features.player.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VideoChapterTest : FunSpec({

    val opening = VideoChapter(index = 0, start = 1_000L, end = 2_000L, type = VideoChapterType.OPENING)
    val part = VideoChapter(index = 1, start = 2_000L, end = 5_000L, type = VideoChapterType.CHAPTER)
    val chapters = listOf(opening, part)

    context("current chapter at a position") {

        test("position inside a chapter resolves to it") {
            chapters.currentAt(1_500L) shouldBe opening
        }

        test("position on a chapter start resolves to that chapter") {
            chapters.currentAt(1_000L) shouldBe opening
        }

        test("position equal to a chapter end resolves to the next chapter") {
            chapters.currentAt(2_000L) shouldBe part
        }

        test("position before the first chapter start resolves to no chapter") {
            chapters.currentAt(999L) shouldBe null
        }

        test("position equal to the last chapter end resolves to no chapter") {
            chapters.currentAt(5_000L) shouldBe null
        }

        test("position after the last chapter end resolves to no chapter") {
            chapters.currentAt(6_000L) shouldBe null
        }

        test("empty chapter list resolves to no chapter") {
            emptyList<VideoChapter>().currentAt(0L) shouldBe null
        }

    }

})
