package com.kaem.flux.model

import java.util.concurrent.TimeUnit

/*data class WatchTime(val time: Long) {

    var hours: Long = 0L
    var minutes: Long = 0L
    var seconds: Long = 0L

    val inMinutes get() = (hours * 60L) + minutes

    init {

        if (time >= 0L) {
            val seconds = time / 1000
            this.hours = seconds / 3600
            this.minutes = (seconds % 3600) / 60
            this.seconds = seconds % 60
        }

    }

    override fun toString(): String {
        return buildString {
            if (hours > 0) append("${hours}h")
            if (minutes > 0) append("${minutes}min")
            if (seconds > 0 || isEmpty()) append("${seconds}sec")
        }
    }

}*/