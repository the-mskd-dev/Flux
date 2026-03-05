package com.kaem.flux.utils.extensions

import android.util.Log
import com.kaem.flux.utils.Constants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.parseTMDBDate() : Date? {

    return try {

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.parse(this)

    } catch (e: ParseException) {

        Log.e("Date Parsing", "Fail to parse date : $this", e)
        null

    }

}

fun String?.uppercaseFirstLetter() : String? {
    return this?.replaceFirstChar { if (it.isLowerCase()) it. titlecase(Locale.getDefault()) else it.toString() }
}

val String.tmdbImage : String get() = Constants.TMDB.IMAGE + this

val String.tmdbImageLarge : String get() = Constants.TMDB.IMAGE_LARGE + this