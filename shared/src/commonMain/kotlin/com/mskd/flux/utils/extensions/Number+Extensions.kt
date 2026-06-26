package com.mskd.flux.utils.extensions

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

fun Long.timeDescription(withoutSeconds: Boolean = false) : String {
    val seconds = this / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}min ")
        if ((remainingSeconds > 0 || isEmpty()) && !withoutSeconds) append("${remainingSeconds}sec")
    }.trim()
}

fun Long.formatMinSec(): String {
    if (this <= 0L) return "00:00"

    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return if (hours > 0) {
        "${hours.pad()}:${remainingMinutes.pad()}:${remainingSeconds.pad()}"
    } else {
        "${minutes.pad()}:${remainingSeconds.pad()}"
    }
}

private fun Long.pad(): String = toString().padStart(2, '0')

val Long.msToMin : Long get() = this.milliseconds.inWholeMinutes
val Int.minToMs : Long get() = this.minutes.inWholeMilliseconds

val Float.toRating : String get() = if (this <= 0f) "-" else "%.2f".format(this).trimEnd('0').trimEnd('.')