package com.kaem.flux.model

import android.os.Parcelable
import com.kaem.flux.model.artwork.ContentType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.regex.Pattern

@Parcelize
@Serializable
data class UserFile(
    val name: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource
) : Parcelable {

    val nameProperties: FileProperties
        get() = FileProperties.extractFileProperties(name)

    val isEpisode: Boolean
        get() = nameProperties.season != null && nameProperties.episode != null

    val addedDate: Date
        get() = Date(addedDateTime)

}

enum class FileSource {
    LOCAL, GOOGLE
}


data class FileProperties(
    val title: String,
    val year: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
) {

    companion object {

        fun extractFileProperties(filename: String): FileProperties {

            // Patterns
            val moviePattern = Pattern.compile("^(.*?)[ .]*(?:\\((\\d{4})\\))?\\.[^.]+$")
            val episodePattern = Pattern.compile(
                "^(.*?)[ ._-]*(?:[sS](\\d{1,2})[ .]*[eE](\\d{1,2})|" +
                        "(\\d{1,2})[xX](\\d{1,2})|" +
                        "season[ .]*(\\d{1,2})[ .]*episode[ .]*(\\d{1,2})|" +
                        "se(\\d{1,2})[ .]*ep(\\d{1,2})).*\\.[^.]+$"
            )

            // Try episode pattern
            val episodeMatcher = episodePattern.matcher(filename)
            if (episodeMatcher.matches()) {
                val title = episodeMatcher.group(1)?.replace("-", " ")?.trim()?.lowercase()
                val season = episodeMatcher.group(2)?.toIntOrNull()
                    ?: episodeMatcher.group(4)?.toIntOrNull()
                    ?: episodeMatcher.group(6)?.toIntOrNull()
                    ?: episodeMatcher.group(8)?.toIntOrNull()
                val episode = episodeMatcher.group(3)?.toIntOrNull()
                    ?: episodeMatcher.group(5)?.toIntOrNull()
                    ?: episodeMatcher.group(7)?.toIntOrNull()
                    ?: episodeMatcher.group(9)?.toIntOrNull()
                return FileProperties(title ?: "", null, season, episode)
            }

            // Try movie pattern
            val movieMatcher = moviePattern.matcher(filename)
            if (movieMatcher.matches()) {
                val title = movieMatcher.group(1)?.replace("-", " ")?.trim()?.lowercase()
                val year = movieMatcher.group(2)?.toIntOrNull()
                return FileProperties(title ?: "", year, null, null)
            }

            // If no pattern works, return the filename as title
            return FileProperties(filename.replace("-", " ").trim().lowercase(), null, null, null)
        }

    }

}

data class UserFolder(
    val title: String,
    val files: List<UserFile>
) {

    val type: ContentType?
        get() = when {
            files.all { it.isEpisode } -> ContentType.SHOW
            files.size == 1 && !files.first().isEpisode -> ContentType.MOVIE
            else -> null
        }

}