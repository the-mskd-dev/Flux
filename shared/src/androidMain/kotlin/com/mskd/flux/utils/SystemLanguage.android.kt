package com.mskd.flux.utils

import java.util.Locale

actual fun systemLanguage(): String {
    return Locale.getDefault().language
}

actual fun languageDisplayName(tag: String): String {
    return Locale.forLanguageTag(tag).displayName
}