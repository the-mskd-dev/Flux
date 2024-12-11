package com.kaem.flux.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(
    tableName = "files",
    foreignKeys = [
        ForeignKey(
            entity = Episode::class,
            parentColumns = ["fileId"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Movie::class,
            parentColumns = ["fileId"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource
) : Parcelable {

    val nameProperties: FileNameProperties
        get() = FileNameProperties.fromFileName(name)

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