package com.mskd.flux.utils.extensions

import android.util.Log
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.utils.Constants
import kotlinx.datetime.LocalDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.parseTMDBDate() : LocalDate? {

    return try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        Log.e("Date Parsing", "Fail to parse date : $this", e)
        null
    }

}

val Media.releaseDate: LocalDate? get() = this.releaseDateString.parseTMDBDate()

fun String?.uppercaseFirstLetter() : String? {
    return this?.replaceFirstChar { if (it.isLowerCase()) it. titlecase(Locale.getDefault()) else it.toString() }
}

val String.tmdbImage : String get() = Constants.TMDB.IMAGE + this

val String.tmdbImageLarge : String get() = Constants.TMDB.IMAGE_LARGE + this