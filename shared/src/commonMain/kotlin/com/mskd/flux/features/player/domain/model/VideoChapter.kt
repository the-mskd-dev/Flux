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

        private val openingRegex = Regex(
            """^\W*(op|opening|ncop|intro(duction)?)(\d|\W|$)""",
            RegexOption.IGNORE_CASE
        )

        private val endingRegex = Regex(
            """^\W*(ed|ending|nced|outro|credits?|end\s+credits?)(\d|\W|$)|^\W*end\W*$""",
            RegexOption.IGNORE_CASE
        )

        fun fromString(value: String?): VideoChapterType {
            val title = value?.trim().orEmpty()
            return when {
                title.isEmpty() -> CHAPTER
                openingRegex.containsMatchIn(title) -> OPENING
                endingRegex.containsMatchIn(title) -> ENDING
                else -> CHAPTER
            }
        }

    }
}
