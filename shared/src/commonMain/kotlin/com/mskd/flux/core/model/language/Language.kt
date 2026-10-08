package com.mskd.flux.core.model.language

enum class Language(val code: String, val region: String) {
    ENGLISH("en", "US"),
    FRENCH("fr", "FR"),
    KOREAN("ko", "KR"),
    JAPANESE("ja", "JP"),
    GERMAN("de", "DE"),
    ITALIAN("it", "IT"),
    SPANISH("es", "ES"),
    PORTUGUESE("pt", "PT"),
    DUTCH("nl", "NM"),
    ARABIC("ar", "SA"),
    CHINESE("zh", "CN")
    ;

    override fun toString(): String = "$code-$region"

    companion object {

        fun fromCode(code: String) : Language = when(code) {
            "fr" -> FRENCH
            "ko" -> KOREAN
            "ja" -> JAPANESE
            "de" -> GERMAN
            "it" -> ITALIAN
            "es" -> SPANISH
            "pt" -> PORTUGUESE
            "nl" -> DUTCH
            "ar" -> ARABIC
            "zh" -> CHINESE
            else -> ENGLISH
        }

    }

}