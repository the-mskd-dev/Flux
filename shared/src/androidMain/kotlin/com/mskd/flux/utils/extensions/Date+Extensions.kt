package com.mskd.flux.utils.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import java.text.DateFormat
import java.util.Date

val LocalDate.formattedText: String get() {
    return format(LocalDate.Format {
        monthName(MonthNames.ENGLISH_FULL)
        char(' ')
        day()
        chars(", ")
        year()
    })
}