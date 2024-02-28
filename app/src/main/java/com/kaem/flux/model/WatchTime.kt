package com.kaem.flux.model

import java.util.concurrent.TimeUnit

data class WatchTime(
    val hours: Long = 0L,
    val min: Long = 0L,
    val sec: Long = 0L,
) {

    val timeInMin = (hours * 60L) + min

    override fun toString(): String {
        return "$hours:$min:$sec"
    }

    companion object {

        fun fromTime(time: Long) : WatchTime {

            if (time < 0L)
                return WatchTime()

            var ms = time

            val hours = TimeUnit.MILLISECONDS.toHours(ms)
            ms -= TimeUnit.HOURS.toMillis(hours)
            val min = TimeUnit.MILLISECONDS.toMinutes(ms)
            ms -= TimeUnit.MINUTES.toMillis(min)
            val sec = TimeUnit.MILLISECONDS.toSeconds(ms)

            return WatchTime(
                hours = hours,
                min = min,
                sec = sec
            )
        }
    }

}