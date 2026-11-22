package com.mskd.flux.features.player.domain.selection

import com.mskd.flux.core.model.language.languageCode

/** Subtitle format, as far as automatic selection cares. */
enum class SubtitleTrackFormat {
    /** Styled text (ASS/SSA): ranked ahead of every other format. */
    ASS_SSA,
    /** SRT, VTT, bitmap (PGS, VobSub, DVB) and anything else. */
    OTHER,
}

/**
 * Plain description of one subtitle Track, free of player types.
 *
 * @param track Caller's handle on the Track, handed back in [SubtitleSelection.Override].
 * @param externalOrder Position of the external subtitle file among those attached to the Media,
 * null for a Track embedded in the UserFile.
 */
data class SubtitleTrackDescription<out T>(
    val track: T,
    val language: String?,
    val externalOrder: Int?,
    val format: SubtitleTrackFormat,
    val isForced: Boolean,
) {
    val isExternal: Boolean get() = externalOrder != null
}

/** Outcome of automatic subtitle selection. */
sealed interface SubtitleSelection<out T> {
    /** Force this Track. */
    data class Override<out T>(val track: T) : SubtitleSelection<T>

    /** Force no Track: only the preferred text language is set and the player chooses. */
    data object PreferredLanguageOnly : SubtitleSelection<Nothing>
}

/** How closely a Track language matches the preferred subtitle language, weakest first. */
internal enum class LanguageMatch {
    NONE,
    /** Same primary language, different or missing region (`pt-BR` vs `pt`). */
    PRIMARY,
    /** Same language tag, case aside. */
    EXACT;

    companion object {
        fun of(trackLanguage: String?, preferredLanguage: String?): LanguageMatch = when {
            trackLanguage.isNullOrBlank() || preferredLanguage.isNullOrBlank() -> NONE
            trackLanguage.equals(preferredLanguage, ignoreCase = true) -> EXACT
            trackLanguage.languageCode().equals(preferredLanguage.languageCode(), ignoreCase = true) -> PRIMARY
            else -> NONE
        }
    }
}

/**
 * Automatic subtitle Track selection, used when the viewer has not tapped a Track in the Settings sheet.
 *
 * Among external Tracks matching the preferred language: exact before primary language match, then
 * ASS/SSA first, non-forced first, external order. When only embedded Tracks match, none is forced
 * so the player honors the container's default and forced flags.
 * When no Track matches, an untagged external Track is selected with the same format ordering.
 */
object SubtitleSelectionPolicy {

    fun <T> select(
        preferredLanguage: String?,
        tracks: List<SubtitleTrackDescription<T>>,
    ): SubtitleSelection<T> {
        val ranked = tracks.map { RankedTrack(it, LanguageMatch.of(it.language, preferredLanguage)) }

        val externalMatch = ranked
            .filter { it.description.isExternal && it.languageMatch != LanguageMatch.NONE }
            .minWithOrNull(compareByDescending<RankedTrack<T>> { it.languageMatch }.then(tieBreakOrder()))
        val anyMatch = ranked.any { it.languageMatch != LanguageMatch.NONE }

        val chosen = when {
            externalMatch != null -> externalMatch
            // Only embedded Tracks match: the container's default and forced flags decide, not us
            anyMatch -> null
            else -> ranked
                .filter { it.description.isExternal && it.description.language.isNullOrBlank() }
                .minWithOrNull(tieBreakOrder())
        }

        return chosen?.let { SubtitleSelection.Override(it.description.track) }
            ?: SubtitleSelection.PreferredLanguageOnly
    }

    private class RankedTrack<T>(
        val description: SubtitleTrackDescription<T>,
        val languageMatch: LanguageMatch,
    )

    private fun <T> tieBreakOrder() = compareByDescending<RankedTrack<T>> { it.description.format == SubtitleTrackFormat.ASS_SSA }
        .thenBy { it.description.isForced }
        .thenBy { it.description.externalOrder }
}
