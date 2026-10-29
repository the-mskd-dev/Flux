package com.mskd.flux.mockups

import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType

object PlayerMockups {

    object Subtitles {

        val english = PlayerTrack(
            id = "subtitles_english",
            label = "English",
            language = "en",
            type = PlayerTrack.Type.SUBTITLES
        )

        val french = PlayerTrack(
            id = "subtitles_french",
            label = "French",
            language = "fr",
            type = PlayerTrack.Type.SUBTITLES
        )

        val german = PlayerTrack(
            id = "subtitles_german",
            label = "German",
            language = "ge",
            type = PlayerTrack.Type.SUBTITLES
        )

        val italian = PlayerTrack(
            id = "subtitles_italian",
            label = "Italian",
            language = "it",
            type = PlayerTrack.Type.SUBTITLES
        )

        val japanese = PlayerTrack(
            id = "subtitles_japanese",
            label = "Japanese",
            language = "ja",
            type = PlayerTrack.Type.SUBTITLES
        )

    }

    object Audio {

        val english = PlayerTrack(
            id = "audio_english",
            label = "English",
            language = "en",
            type = PlayerTrack.Type.AUDIO
        )

        val french = PlayerTrack(
            id = "audio_french",
            label = "French",
            language = "fr",
            type = PlayerTrack.Type.AUDIO
        )

        val german = PlayerTrack(
            id = "audio_german",
            label = "German",
            language = "de",
            type = PlayerTrack.Type.AUDIO
        )

        val italian = PlayerTrack(
            id = "audio_italian",
            label = "Italian",
            language = "it",
            type = PlayerTrack.Type.AUDIO
        )

        val japanese = PlayerTrack(
            id = "audio_japanese",
            label = "Japanese",
            language = "ja",
            type = PlayerTrack.Type.AUDIO
        )

    }

    val tracks = listOf(
        Subtitles.english,
        Subtitles.french,
        Subtitles.german,
        Subtitles.italian,
        Subtitles.japanese,
        Audio.english,
        Audio.french,
        Audio.german,
        Audio.italian,
        Audio.japanese
    )

    val chapters = listOf(
        VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING, title = "Prologue"),
        VideoChapter(index = 1, start = 90_000L, end = 600_000L, type = VideoChapterType.CHAPTER, title = "Part A"),
        VideoChapter(index = 2, start = 600_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER),
        VideoChapter(index = 3, start = 1_200_000L, end = 1_380_000L, type = VideoChapterType.ENDING, title = "Preview"),
    )

}
