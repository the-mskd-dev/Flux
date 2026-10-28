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
            """^\W*(mixed\s+op|op|opening|ncop|intro(duction)?)(?!\p{L})|(?<!\p{L})op$|オープニング|^ＯＰ""",
            RegexOption.IGNORE_CASE
        )

        private val endingRegex = Regex(
            """^\W*(ed|ending|nced|outro|credits?\s+start|end\s+credits?)(?!\p{L})|^\W*credits?\W*\d*$|(?<!\p{L})ed$|^\W*end\W*$|エンディング|^ＥＤ""",
            RegexOption.IGNORE_CASE
        )

        private val exclusionRegex = Regex(
            """(?<!\p{L})(op|opening|ncop|ed|ending|nced|credits?\s+start)\d*[\s:]+end\W*$|post\W*credit|credits?\s+scene""",
            RegexOption.IGNORE_CASE
        )

        fun fromString(value: String?): VideoChapterType {
            val title = value?.trim().orEmpty()
            return when {
                title.isEmpty() -> CHAPTER
                exclusionRegex.containsMatchIn(title) -> CHAPTER
                openingRegex.containsMatchIn(title) -> OPENING
                endingRegex.containsMatchIn(title) -> ENDING
                else -> CHAPTER
            }
        }

    }
}
