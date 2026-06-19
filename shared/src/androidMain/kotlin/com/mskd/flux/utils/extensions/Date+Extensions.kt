package com.mskd.flux.utils.extensions

import java.text.DateFormat
import java.util.Date

val Date.formattedText : String get() {
    return DateFormat.getDateInstance().format(this)
}