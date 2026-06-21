package com.mskd.flux.utils.extensions

import com.mskd.flux.shared.utils.Trace
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val LocalDate.formattedText: String? get() {
    try {
        val formatter = DateTimeFormatter
            .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
        return this.toJavaLocalDate().format(formatter)
    } catch (e: Exception) {
        Trace.error("LocalDate", "Fail to format text for $this", e)
        return null
    }
}