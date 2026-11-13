package com.mskd.flux.features.player.domain.model

data class VideoChapter(
    val index: Int,
    val start: Long,
    val end: Long,
    val type: VideoChapterType,
    /** Title read from the UserFile, `null` when it has none (blank included). */
    val title: String? = null
)

/**
 * The Chapter playing at [position]. A Chapter covers `[start, end)`: a position on a boundary
 * belongs to the Chapter that starts there. Before the first start or from the last end, there is none.
 */
fun List<VideoChapter>.currentAt(position: Long): VideoChapter? =
    firstOrNull { position >= it.start && position < it.end }

/**
 * Positions (as a fraction of [duration], strictly between 0 and 1) where the progress bar is cut:
 * the start of each Chapter but the first. Empty with fewer than 2 Chapters, so the bar stays continuous.
 */
fun List<VideoChapter>.boundaryFractions(duration: Long): List<Float> {
    if (size < 2 || duration <= 0L) return emptyList()
    return drop(1)
        .map { it.start.toFloat() / duration }
        .filter { it > 0f && it < 1f }
        .distinct()
        .sorted()
}

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
