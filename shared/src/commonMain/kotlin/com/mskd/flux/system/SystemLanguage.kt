package com.mskd.flux.system

expect fun systemLanguage(): String
expect fun systemLanguageCode() : String
expect fun systemLanguageRegion() : String

expect fun languageDisplayName(code: String): String