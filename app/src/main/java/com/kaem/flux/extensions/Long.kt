package com.kaem.flux.extensions

import java.util.concurrent.TimeUnit

val Long.timeString : String get() {

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