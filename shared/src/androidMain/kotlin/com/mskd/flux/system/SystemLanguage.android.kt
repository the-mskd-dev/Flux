package com.mskd.flux.system

import java.util.Locale

actual fun systemLanguage(): String {
    return systemLanguageCode() + "-" + systemLanguageRegion()
}

actual fun systemLanguageCode(): String {
    return Locale.getDefault().language
}

actual fun systemLanguageRegion(): String {
    return Locale.getDefault().country
}

actual fun languageDisplayName(code: String): String {
    return Locale.forLanguageTag(code).displayName
}