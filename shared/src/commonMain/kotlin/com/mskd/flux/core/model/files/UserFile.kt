package com.mskd.flux.core.model.files

import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath
import kotlin.time.Instant

@Serializable
data class UserFile(
    val name: String,
    val addedDateTime: Long = 0L,
    val path: String,
    val realPath: String = "",
    val source: FileSource = FileSource.LOCAL,
    val parentDocId: String? = null
) {

    val nameProperties: FileProperties
        get() = FileProperties.extractFromName(name)
            ?: FileProperties.extractFromPath(realPath)
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
            Regex("^ep(?:isode)?[ .]*(\\d{1,4})$", RegexOption.IGNORE_CASE),    // episode 02 / episode2 / ep2
            Regex("^e(\\d{1,4})$", RegexOption.IGNORE_CASE),                    // e02
            Regex("^(\\d{1,4})$"),                                                      // 02
        )

        private val SEASON_ONLY_PATTERNS = listOf(
            Regex("^season[ .]*(\\d{1,2})$", RegexOption.IGNORE_CASE),
            Regex("^s ?(\\d{1,2})$", RegexOption.IGNORE_CASE),
            Regex("^(\\d{1,2})$"),
        )

        private val YEAR_PATTERN = Regex("\\((\\d{4})\\)|(?:^|[ ._-])(\\d{4})(?=[ ._-]|$)")

        private val NUMERIC_ONLY = Regex("^\\d+$")

        private val RELEASE_TAG_BOUNDARY = Regex(
            "(?i)[ ._-](?:multi|vostfr|truefrench|vff?\\d?|french|\\d{3,4}p|4k|" +
                    "web-?dl|webrip|bluray|bdrip|dvdrip|hdtv|hdlight|" +
                    "x264|x265|h26[45]|hevc|aac\\d?|dts|ac3)\\b"
        )

        private val NON_TITLE_FOLDERS = setOf("movies", "movie", "download", "downloads", "flux")


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

            // 1. Remove tags
            val boundaryMatch = RELEASE_TAG_BOUNDARY.find(raw)
            val relevant = if (boundaryMatch != null) raw.substring(0, boundaryMatch.range.first) else raw

            // 2. Try to find a year
            val yearMatch = YEAR_PATTERN.find(relevant)?.takeIf { it.range.first > 0 }
            val year = yearMatch?.let { it.groupValues[1].toIntOrNull() ?: it.groupValues[2].toIntOrNull() }
            val titleRaw = if (yearMatch != null) relevant.substring(0, yearMatch.range.first) else relevant

            // 3. Clean title
            val title = titleRaw
                .replace("-", " ")
                .replace("_", " ")
                .replace(".", " ")
                .trim(' ', '.', '_')
                .lowercase()
                .replace(Regex(" +"), " ")

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

            // If only numeric, maybe a naming by folder
            if (NUMERIC_ONLY.matches(nameWithoutExt)) return null

            // Try for movie
            val (title, year) = extractTitleAndYear(nameWithoutExt)
            return if (title.isNotBlank()) FileProperties(title, year, null, null) else null
        }

        fun extractFromPath(path: String): FileProperties? {
            val segments = path.toPath().segments
            val lastSegment = segments.lastOrNull() ?: return null
            val filename = lastSegment.substringBeforeLast('.', lastSegment)

            // 1. Season + episode in the filename
            findSeasonEpisode(filename)?.let { (season, episode) ->
                val titleSegment = segments.getOrNull(segments.size - 2)
                    ?.takeUnless { it.lowercase() in NON_TITLE_FOLDERS }
                    ?: return null // no title folder
                val (title, year) = extractTitleAndYear(titleSegment)
                return FileProperties(title, year, season, episode)
            }

            // 2. Episode only -> need season in the parent folder
            // if none, it's a movie
            for (pattern in EPISODE_ONLY_PATTERNS) {
                val episodeMatch = pattern.matchEntire(filename) ?: continue
                val episode = episodeMatch.groupValues[1].toIntOrNull() ?: continue

                val parentFolder = segments.getOrNull(segments.size - 2)
                val season = parentFolder?.let { findSeason(it) }

                if (season == null) break // no season -> no need to continue here

                val titleSegment = segments.getOrNull(segments.size - 3) ?: return null
                val (title, year) = extractTitleAndYear(titleSegment)
                return FileProperties(title, year, season, episode)
            }

            // 3. No episode -> movie
            // Specific case : filename only numeric without season folder -> the number is the title
            if (NUMERIC_ONLY.matches(filename)) {
                return FileProperties(title = filename, year = null, season = null, episode = null)
            }

            val parentFolder = segments.getOrNull(segments.size - 2) ?: return null
            val (title, year) = extractTitleAndYear(parentFolder)
            return FileProperties(title, year, null, null)
        }

    }

}