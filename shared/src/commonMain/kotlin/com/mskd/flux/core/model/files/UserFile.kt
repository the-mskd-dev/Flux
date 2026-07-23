package com.mskd.flux.core.model.files

import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import kotlin.time.Instant

@Serializable
data class UserFile(
    val name: String,
    val addedDateTime: Long = 0L,
    val path: String,
    val source: FileSource = FileSource.LOCAL,
    val parentDocId: String? = null
) {

    val nameProperties: FileProperties
        get() = FileProperties.extractFromName(name)
            ?: FileProperties.extractFromPath(path)
            ?: FileProperties(title = name.replace("-", " ").trim().lowercase())

    val isEpisode: Boolean
        get() = nameProperties.season != null && nameProperties.episode != null

    val season: Int?
        get() = nameProperties.season

    val episode: Int?
        get() = nameProperties.episode

    val addedDate: Instant
        get() = Instant.fromEpochMilliseconds(addedDateTime)

}

enum class FileSource {
    LOCAL, SAF,
}

data class FileProperties(
    val title: String,
    val year: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
) {

    companion object {

        private val SEASON_EPISODE_PATTERNS = listOf(
            Regex("s(\\d{1,2})[ .]*e(\\d{1,4})", RegexOption.IGNORE_CASE),
            Regex("(\\d{1,2})x(\\d{1,4})", RegexOption.IGNORE_CASE),
            Regex("se(\\d{1,2})[ .]*ep(\\d{1,4})", RegexOption.IGNORE_CASE),
            Regex("season[ .]*(\\d{1,2})[ .]*episode[ .]*(\\d{1,4})", RegexOption.IGNORE_CASE),
        )

        private val EPISODE_ONLY_PATTERNS = listOf(
            Regex("^ep(?:isode)?[ .]*(\\d{1,4})$", RegexOption.IGNORE_CASE), // episode 02 / episode2 / ep2
            Regex("^e(\\d{1,4})$", RegexOption.IGNORE_CASE),                 // e02
            Regex("^(\\d{1,4})$"),                                          // 02
        )

        private val SEASON_ONLY_PATTERNS = listOf(
            Regex("^season[ .]*(\\d{1,2})$", RegexOption.IGNORE_CASE),
            Regex("^s[ ]?(\\d{1,2})$", RegexOption.IGNORE_CASE),
        )

        private val YEAR_PATTERN = Regex("\\((\\d{4})\\)")

        private fun findSeasonEpisode(text: String): Pair<Int, Int>? {
            for (pattern in SEASON_EPISODE_PATTERNS) {
                val match = pattern.find(text) ?: continue
                val season = match.groupValues[1].toIntOrNull() ?: continue
                val episode = match.groupValues[2].toIntOrNull() ?: continue
                return season to episode
            }
            return null
        }

        private fun findSeasonEpisodeWithRange(text: String): Pair<MatchResult, Pair<Int, Int>>? {
            for (pattern in SEASON_EPISODE_PATTERNS) {
                val match = pattern.find(text) ?: continue
                val season = match.groupValues[1].toIntOrNull() ?: continue
                val episode = match.groupValues[2].toIntOrNull() ?: continue
                return match to (season to episode)
            }
            return null
        }

        private fun findSeason(folderName: String): Int? {
            val trimmed = folderName.trim()
            for (pattern in SEASON_ONLY_PATTERNS) {
                val match = pattern.matchEntire(trimmed) ?: continue
                return match.groupValues[1].toIntOrNull()
            }
            return null
        }

        private fun extractTitleAndYear(raw: String): Pair<String, Int?> {
            val year = YEAR_PATTERN.find(raw)?.groupValues?.get(1)?.toIntOrNull()
            val title = raw
                .replace(YEAR_PATTERN, "")
                .replace("-", " ")
                .replace("_", " ")
                .trim(' ', '.', '_')
                .lowercase()
            return title to year
        }

        fun extractFromName(filename: String): FileProperties? {
            val nameWithoutExt = filename.substringBeforeLast('.', filename)

            // Try for show
            val match = findSeasonEpisodeWithRange(nameWithoutExt)
            if (match != null) {
                val (matchResult, seasonEpisode) = match
                val prefix = nameWithoutExt.substring(0, matchResult.range.first)
                val (title, year) = extractTitleAndYear(prefix)
                if (title.isBlank()) return null
                return FileProperties(title, year, seasonEpisode.first, seasonEpisode.second)
            }

            // Try for movie
            val (title, year) = extractTitleAndYear(nameWithoutExt)
            return if (title.isNotBlank()) FileProperties(title, year, null, null) else null
        }

        fun extractFromPath(path: String) : FileProperties? {
            val segments = path.toPath().segments
            val lastSegment = segments.lastOrNull() ?: return null
            val filename = lastSegment.substringBeforeLast('.', lastSegment)

            findSeasonEpisode(filename)?.let { (season, episode) ->
                val titleSegment = segments.getOrNull(segments.size - 2) ?: return null
                val (title, year) = extractTitleAndYear(titleSegment)
                return FileProperties(title, year, season, episode)
            }

            for (pattern in EPISODE_ONLY_PATTERNS) {
                val episodeMatch = pattern.matchEntire(filename) ?: continue
                val episode = episodeMatch.groupValues[1].toIntOrNull() ?: continue
                val season = segments.getOrNull(segments.size - 2)?.let { findSeason(it) }
                val titleSegment = segments.getOrNull(segments.size - 3) ?: return null
                val (title, year) = extractTitleAndYear(titleSegment)
                return FileProperties(title, year, season, episode)
            }

            val parentFolder = segments.getOrNull(segments.size - 2) ?: return null
            val (title, year) = extractTitleAndYear(parentFolder)
            return FileProperties(title, year, null, null)
        }

    }

}