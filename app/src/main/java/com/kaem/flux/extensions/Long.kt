package com.kaem.flux.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

val Long.timeString : String @SuppressLint("SimpleDateFormat")
get() {

    if (this < 0)
        return "00:00:00"

    var ms = this

    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    ms -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    ms -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)

    return "$hours:$minutes:$seconds"
}