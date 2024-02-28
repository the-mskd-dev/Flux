package com.kaem.flux.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

            val date = SimpleDateFormat("hh:ss:mm", Locale.getDefault()).format(Date(time))
            val parsedDate = date.split(":")

            return WatchTime(
                hours = parsedDate.getOrNull(0)?.toLongOrNull()?: 0L,
                min = parsedDate.getOrNull(1)?.toLongOrNull()?: 0L,
                sec = parsedDate.getOrNull(2)?.toLongOrNull()?: 0L,
            )
        }
    }

}