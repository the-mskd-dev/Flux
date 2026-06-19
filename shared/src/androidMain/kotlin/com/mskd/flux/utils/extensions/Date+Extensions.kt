package com.mskd.flux.utils.extensions

import android.util.Log
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaLocalDate
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

val LocalDate.formattedText: String? get() {
    try {
        val formatter = DateTimeFormatter
            .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
        return this.toJavaLocalDate().format(formatter)
    } catch (e: Exception) {
        Log.d("LocalDate", "Fail to format text for $this", e)
        return null
    }
}