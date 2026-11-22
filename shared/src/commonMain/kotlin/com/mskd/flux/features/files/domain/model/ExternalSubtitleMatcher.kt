package com.mskd.flux.features.files.domain.model

data class SubtitleCandidate(
    val uri: String,
    val fileName: String,
)

object ExternalSubtitleMatcher {

    val SUPPORTED_EXTENSIONS = setOf("ass", "ssa", "srt", "vtt")

    val ORDER: Comparator<ExternalSubtitle> = compareBy<ExternalSubtitle>(
        { it.formatPriority },
        { it.isForced },
        { it.fileName.lowercase() },
        { it.fileName },
    )

    private val ISO_639_1 = setOf(
        "en", "fr", "de", "es", "it", "pt", "ja", "ko", "zh", "ru",
        "ar", "nl", "pl", "tr", "vi", "th", "id", "sv", "no", "nb",
        "nn", "da", "fi", "cs", "hu", "ro", "el", "he", "uk", "hi",
        "bn", "ms", "tl", "ca", "hr", "sr", "sk", "sl", "bg", "lt",
        "lv", "et", "fa", "ur", "ta", "te", "ml", "mr", "gu", "kn",
        "pa", "eu", "gl", "is", "ga", "cy", "sq", "hy", "ka", "mk",
        "bs", "az", "kk", "uz", "mn", "my", "km", "lo", "ne", "si",
        "sw", "af", "am", "mt", "la", "eo", "be", "ku", "jv", "bo",
    )

    private val ISO_639_2_TO_1 = mapOf(
        "eng" to "en",
        "fre" to "fr", "fra" to "fr",
        "ger" to "de", "deu" to "de",
        "spa" to "es",
        "ita" to "it",
        "por" to "pt",
        "jpn" to "ja",
        "kor" to "ko",
        "chi" to "zh", "zho" to "zh",
        "rus" to "ru",
        "ara" to "ar",
        "dut" to "nl", "nld" to "nl",
        "pol" to "pl",
        "tur" to "tr",
        "vie" to "vi",
        "tha" to "th",
        "ind" to "id",
        "swe" to "sv",
        "nor" to "no",
        "nob" to "nb",
        "nno" to "nn",
        "dan" to "da",
        "fin" to "fi",
        "cze" to "cs", "ces" to "cs",
        "hun" to "hu",
        "rum" to "ro", "ron" to "ro",
        "gre" to "el", "ell" to "el",
        "heb" to "he",
        "ukr" to "uk",
        "hin" to "hi",
        "ben" to "bn",
        "may" to "ms", "msa" to "ms",
        "tgl" to "tl",
        "fil" to "fil",
        "cat" to "ca",
        "hrv" to "hr",
        "srp" to "sr",
        "slo" to "sk", "slk" to "sk",
        "slv" to "sl",
        "bul" to "bg",
        "lit" to "lt",
        "lav" to "lv",
        "est" to "et",
        "per" to "fa", "fas" to "fa",
        "urd" to "ur",
        "tam" to "ta",
        "tel" to "te",
        "mal" to "ml",
        "mar" to "mr",
        "guj" to "gu",
        "kan" to "kn",
        "pan" to "pa",
        "baq" to "eu", "eus" to "eu",
        "glg" to "gl",
        "ice" to "is", "isl" to "is",
        "gle" to "ga",
        "wel" to "cy", "cym" to "cy",
        "alb" to "sq", "sqi" to "sq",
        "arm" to "hy", "hye" to "hy",
        "geo" to "ka", "kat" to "ka",
        "mac" to "mk", "mkd" to "mk",
        "bos" to "bs",
        "aze" to "az",
        "kaz" to "kk",
        "uzb" to "uz",
        "mon" to "mn",
        "bur" to "my", "mya" to "my",
        "khm" to "km",
        "lao" to "lo",
        "nep" to "ne",
        "sin" to "si",
        "swa" to "sw",
        "afr" to "af",
        "amh" to "am",
        "mlt" to "mt",
        "lat" to "la",
        "epo" to "eo",
        "bel" to "be",
        "kur" to "ku",
        "jav" to "jv",
        "tib" to "bo", "bod" to "bo",
    )

    private val KNOWN_SCRIPTS = setOf(
        "hans", "hant", "latn", "cyrl", "arab", "jpan", "kore", "deva", "grek", "hebr"
    )

    private val NON_REGION_SUBTAGS = setOf("cc", "hi")

    fun matchAll(
        userFileName: String,
        candidates: Iterable<SubtitleCandidate>,
    ): List<ExternalSubtitle> =
        candidates
            .mapNotNull { candidate ->
                match(
                    userFileName = userFileName,
                    candidateFileName = candidate.fileName,
                    uri = candidate.uri,
                )
            }
            .sortedWith(ORDER)

    fun match(
        userFileName: String,
        candidateFileName: String,
        uri: String,
    ): ExternalSubtitle? {
        val cleanUserFileName = userFileName.substringAfterLast('/')
        val userFileBase = cleanUserFileName.substringBeforeLast('.', cleanUserFileName)
        if (userFileBase.isBlank()) return null

        val cleanCandidate = candidateFileName.substringAfterLast('/')
        val lastDot = cleanCandidate.lastIndexOf('.')
        if (lastDot <= 0) return null

        val extension = cleanCandidate.substring(lastDot + 1).lowercase()
        if (extension !in SUPPORTED_EXTENSIONS) return null

        val candidateBase = cleanCandidate.substring(0, lastDot)
        val rawTokens = when {
            candidateBase.equals(userFileBase, ignoreCase = true) -> emptyList()
            candidateBase.length > userFileBase.length + 1 &&
                candidateBase.startsWith(userFileBase, ignoreCase = true) &&
                candidateBase[userFileBase.length] == '.' -> {
                val suffix = candidateBase.substring(userFileBase.length + 1)
                val tokens = suffix.split('.')
                if (tokens.isEmpty() || tokens.any { it.isBlank() }) return null
                tokens
            }
            else -> return null
        }

        var language: String? = null
        val tags = mutableListOf<String>()
        for (token in rawTokens) {
            if (language == null) {
                val parsed = parseLanguageTag(token)
                if (parsed != null) {
                    language = parsed
                    continue
                }
            }
            tags += token
        }

        return ExternalSubtitle(
            uri = uri,
            fileName = cleanCandidate,
            extension = extension,
            language = language,
            tags = tags,
        )
    }

    private fun parseLanguageTag(token: String): String? {
        val parts = token.split('-', '_')
        return when (parts.size) {
            1 -> normalizePrimaryLanguage(parts[0])
            2 -> {
                val lang = normalizePrimaryLanguage(parts[0]) ?: return null
                val second = parts[1]
                val secondLower = second.lowercase()
                when {
                    secondLower in KNOWN_SCRIPTS -> {
                        "$lang-${secondLower.replaceFirstChar { it.uppercaseChar() }}"
                    }
                    isValidRegion(second) -> "$lang-${second.uppercase()}"
                    else -> null
                }
            }
            3 -> {
                val lang = normalizePrimaryLanguage(parts[0]) ?: return null
                val scriptLower = parts[1].lowercase()
                val region = parts[2]
                if (scriptLower !in KNOWN_SCRIPTS || !isValidRegion(region)) return null
                val script = scriptLower.replaceFirstChar { it.uppercaseChar() }
                "$lang-$script-${region.uppercase()}"
            }
            else -> null
        }
    }

    private fun normalizePrimaryLanguage(code: String): String? {
        val lower = code.lowercase()
        return when {
            lower == "iw" -> "he"
            lower in ISO_639_1 -> lower
            else -> ISO_639_2_TO_1[lower]
        }
    }

    private fun isValidRegion(subtag: String): Boolean {
        val lower = subtag.lowercase()
        val isTwoLetterRegion = subtag.length == 2 &&
            subtag.all { it in 'a'..'z' || it in 'A'..'Z' } &&
            lower !in NON_REGION_SUBTAGS
        val isThreeDigitRegion = subtag.length == 3 && subtag.all { it in '0'..'9' }
        return isTwoLetterRegion || isThreeDigitRegion
    }
}
