package com.kaem.flux.model

import android.net.Uri
import java.util.Date

data class UserFile(
    val name: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource
) {

    @Transient
    val nameProperties: FileNameProperties = FileNameProperties.fromFileName(name)

    @Transient
    val addedDate: Date = Date(addedDateTime)

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

            var name = fileName.substringBeforeLast('.').lowercase()
            var season: Int? = null
            var episode: Int? = null
            var year: Int? = null

            val splitName = name.split('_')
            if (splitName.size == 2) {

                name = splitName[0]

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

val fileName1 = "spy x family_s02e03.mkv"
val fileName2 = "spiderman (2002).mkv"
val fileName3 = "naruto.mkv"
val fileName4 = "spiderman (2017)_s02e04.mkv"
val fileName5 = "spiderman_2017.mkv"
val fileName6 = "2001 : L'odyssée de l'espace.mkv"