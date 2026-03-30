package com.mskd.flux.mockups

import com.mskd.flux.screens.player.PlayerTrack

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

}