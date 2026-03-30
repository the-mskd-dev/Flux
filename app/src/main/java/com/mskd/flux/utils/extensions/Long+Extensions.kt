package com.mskd.flux.utils.extensions

import java.util.Locale
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
    return if (this <= 0L) "00:00"
    else {
        val totalSeconds = this / 1000
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
        } else {
            String.format(Locale.getDefault(),"%02d:%02d", minutes, remainingSeconds)
        }

    }
}

val Long.msToMin : Long get() = this.milliseconds.inWholeMinutes
val Int.minToMs : Long get() = this.minutes.inWholeMilliseconds