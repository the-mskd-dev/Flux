package com.kaem.flux.model

import android.os.Parcelable
import com.kaem.flux.model.artwork.ContentType
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class UserFile(
    val name: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource
) : Parcelable {

    val nameProperties: FileNameProperties
        get() = FileNameProperties.fromFileName(name)

    val isEpisode: Boolean
        get() = nameProperties.season != null && nameProperties.episode != null

    val addedDate: Date
        get() = Date(addedDateTime)

}

enum class FileSource {
    LOCAL, GOOGLE
}


data class FileNameProperties(
    val title: String,
    val year: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
) {

    companion object {

        fun fromFileName(fileName: String): FileNameProperties {

            var name = fileName
                .substringBeforeLast('.')
                .lowercase()

            var season: Int? = null
            var episode: Int? = null
            var year: Int? = null

            val splitName = name.split('_')
            if (splitName.size == 2) {

                name = splitName[0].trim()

                val seasonAndEpisodeRegex = Regex("s(\\d+)e(\\d+)")
                val seasonAndEpisodeResult = seasonAndEpisodeRegex.find(splitName[1])
                seasonAndEpisodeResult?.let {
                    val (first, last) = it.destructured
                    season = first.toIntOrNull()
                    episode = last.toIntOrNull()
                }

            }

            val yearRegex = Regex("\\b\\d{4}\\b")
            year = yearRegex.find(name)?.value?.toIntOrNull()

            return FileNameProperties(
                title = name,
                season = season,
                episode = episode,
                year = year
            )

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