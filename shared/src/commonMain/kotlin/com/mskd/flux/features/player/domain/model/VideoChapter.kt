package com.mskd.flux.features.player.domain.model

data class VideoChapter(
    val index: Int,
    val start: Long,
    val end: Long,
    val type: VideoChapterType
)

enum class VideoChapterType {
    OPENING, ENDING, CHAPTER;

    companion object {

        fun fromString(value: String?): VideoChapterType {
            return when (value) {
                "opening" -> OPENING
                "ending" -> ENDING
                else -> CHAPTER
            }
        }

    }
}
