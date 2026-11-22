package com.mskd.flux.features.files.domain.model

data class ExternalSubtitle(
    val uri: String,
    val fileName: String,
    val extension: String,
    val language: String? = null,
    val tags: List<String> = emptyList(),
) {
    val isAssOrSsa: Boolean
        get() = extension.equals("ass", ignoreCase = true) || extension.equals("ssa", ignoreCase = true)

    /** Lower value means higher priority: ASS/SSA (0) before SRT/VTT (1). */
    val formatPriority: Int
        get() = if (isAssOrSsa) 0 else 1

    val isForced: Boolean
        get() = tags.any { it.equals("forced", ignoreCase = true) }

    val extensionLabel: String
        get() = extension.uppercase()

    fun formatLabel(
        externalFallback: String,
        languageDisplayName: (String) -> String,
    ): String {
        val primary = language
            ?.let(languageDisplayName)
            ?.takeIf { it.isNotBlank() }
            ?: externalFallback
        val prefix = (listOf(primary) + tags).joinToString(" · ")
        return "$prefix ($extensionLabel)"
    }
}
