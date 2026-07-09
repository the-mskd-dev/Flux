package com.mskd.flux.utils.extensions

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.Trace
import kotlinx.datetime.LocalDate
import java.util.Locale

fun String.parseTMDBDate() : LocalDate? {

    return try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        Trace.error("Date Parsing", "Fail to parse date : $this", e)
        null
    }

}

val Media.releaseDate: LocalDate? get() = this.releaseDateString.parseTMDBDate()

fun String?.uppercaseFirstLetter() : String? {
    return this?.replaceFirstChar { if (it.isLowerCase()) it. titlecase(Locale.getDefault()) else it.toString() }
}

val String.tmdbImage : String get() = Constants.TMDB.IMAGE + this

val String.tmdbImageLarge : String get() = Constants.TMDB.IMAGE_LARGE + this