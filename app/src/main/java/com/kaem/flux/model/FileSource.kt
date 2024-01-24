package com.kaem.flux.model

import android.net.Uri
import java.util.regex.Pattern

sealed class FileSource(
    val name: String,
    val addedDateString: String
) {

    class Local(name: String, addedDateString: String, val uri: Uri) : FileSource(name, addedDateString)
    class GDrive(name: String, addedDateString: String, val path: String) : FileSource(name, addedDateString)

    val nameProperties: FileNameProperties
        get() {

            val regex = Regex("""^(.+?)(?:\s*\((\d{4})\))?(?:_s(\d{1,2})e(\d{1,2}))?.*""")
            val matchResult = regex.matchEntire(name)

            return if (matchResult != null) {
                val (name, year, season, episode) = matchResult.destructured
                FileNameProperties(
                    title = name.trim(),
                    year = year.ifEmpty { null },
                    season = season.toIntOrNull(),
                    episode = episode.toFloatOrNull()
                )
            } else {
                // If no match, return FileNameProperties with name only
                FileNameProperties(title = name.trim())
            }

        }

}

data class FileNameProperties(
    val title: String,
    val year: String? = null,
    val season: Int? = null,
    val episode: Float? = null,
)