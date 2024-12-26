package com.kaem.flux.utils

import kotlin.time.Duration.Companion.milliseconds

val Long.timeDescription : String
    get() {
        val seconds = this / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return buildString {
            if (hours > 0) append("${hours}h")
            if (minutes > 0) append("${minutes}min")
            if (remainingSeconds > 0 || isEmpty()) append("${remainingSeconds}sec")
        }
    }

val Long.inMinutes : Long get() = this.milliseconds.inWholeMinutes