package com.mskd.flux.features.player.domain.model

data class VideoChapter(
    val index: Int,
    val start: Long,
    val end: Long,
    val type: VideoChapterType
) {

    val skippable: Boolean
        get() = this.type != VideoChapterType.CHAPTER

}

enum class VideoChapterType {
    INTRO, OPENING, ENDING, CHAPTER;

    companion object {

        fun fromString(value: String?): VideoChapterType {
            return when (value) {
                in listOf("intro") -> INTRO
                in listOf("op", "opening") -> OPENING
                in listOf("end", "ending, credits") -> ENDING
                else -> CHAPTER
            }
        }

    }
}
