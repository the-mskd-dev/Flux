package com.mskd.flux.core.domain.model.files

import kotlinx.serialization.Serializable
import java.util.Date
import java.util.regex.Pattern

@Serializable
data class UserFile(
    val name: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource
) {

    val nameProperties: FileProperties
        get() = FileProperties.extractFileProperties(name)

    val isEpisode: Boolean
        get() = nameProperties.season != null && nameProperties.episode != null

    val season: Int?
        get() = nameProperties.season

    val episode: Int?
        get() = nameProperties.episode

    val addedDate: Date
        get() = Date(addedDateTime)

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

        fun extractFileProperties(filename: String): FileProperties {

            // Patterns
            val moviePattern = Regex("^(.*?)[ .]*(?:\\((\\d{4})\\))?\\.[^.]+$")
            val episodePattern = Regex(
                "^(.*?)[ ._-]*(?:\\((\\d{4})\\))?[ ._-]*(?:[sS](\\d{1,2})[ .]*[eE](\\d{1,4})|" +
                        "(\\d{1,2})[xX](\\d{1,4})|" +
                        "season[ .]*(\\d{1,2})[ .]*episode[ .]*(\\d{1,4})|" +
                        "se(\\d{1,2})[ .]*ep(\\d{1,4})).*\\.[^.]+$",
            )

            // Try episode pattern
            val episodeMatch = episodePattern.matchEntire(filename)
            episodeMatch?.groupValues?.let { groups ->
                val title = groups[1].replace("-", " ").trim().lowercase()
                val year = groups[2].toIntOrNull()
                val season = groups[3].toIntOrNull()
                    ?: groups[5].toIntOrNull()
                    ?: groups[7].toIntOrNull()
                    ?: groups[9].toIntOrNull()
                val episode = groups[4].toIntOrNull()
                    ?: groups[6].toIntOrNull()
                    ?: groups[8].toIntOrNull()
                    ?: groups[10].toIntOrNull()
                return FileProperties(title ?: "", year, season, episode)
            }

            // Try movie pattern
            val movieMatch = moviePattern.matchEntire(filename)
            movieMatch?.groupValues?.let { groups ->
                val title = groups[1].replace("-", " ").trim().lowercase()
                val year = groups[2].toIntOrNull()
                return FileProperties(title, year, null, null)
            }

            // If no pattern works, return the filename as title
            return FileProperties(filename.replace("-", " ").trim().lowercase(), null, null, null)
        }

    }

}