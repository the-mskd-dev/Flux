package com.kaem.flux.utils.extensions

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

val Long.msToMin : Long get() = this.milliseconds.inWholeMinutes
val Int.minToMs : Long get() = this.minutes.inWholeMilliseconds