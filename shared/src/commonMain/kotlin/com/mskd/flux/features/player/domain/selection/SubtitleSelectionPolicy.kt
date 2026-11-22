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
 * @param isDefault Container default flag, not used by the policy yet.
 */
data class SubtitleTrackDescription<out T>(
    val track: T,
    val language: String?,
    val externalOrder: Int?,
    val format: SubtitleTrackFormat,
    val isForced: Boolean,
    val isDefault: Boolean,
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
 * Among Tracks matching the preferred language: external before embedded, then exact before primary
 * language match, then ASS/SSA first, non-forced first, external order, list position.
 * When no Track matches, an untagged external Track is selected with the same format ordering.
 */
object SubtitleSelectionPolicy {

    fun <T> select(
        preferredLanguage: String?,
        tracks: List<SubtitleTrackDescription<T>>,
    ): SubtitleSelection<T> {
        val ranked = tracks.mapIndexed { position, description ->
            RankedTrack(
                description = description,
                position = position,
                languageMatch = LanguageMatch.of(description.language, preferredLanguage),
            )
        }

        val matchingPreferred = ranked.filter { it.languageMatch != LanguageMatch.NONE }
        val chosen = if (matchingPreferred.isNotEmpty()) {
            matchingPreferred.minWithOrNull(
                compareByDescending<RankedTrack<T>> { it.description.isExternal }
                    .thenByDescending { it.languageMatch }
                    .then(tieBreakOrder())
            )
        } else {
            ranked
                .filter { it.description.isExternal && it.description.language.isNullOrBlank() }
                .minWithOrNull(tieBreakOrder())
        }

        return chosen?.let { SubtitleSelection.Override(it.description.track) }
            ?: SubtitleSelection.PreferredLanguageOnly
    }

    private class RankedTrack<T>(
        val description: SubtitleTrackDescription<T>,
        val position: Int,
        val languageMatch: LanguageMatch,
    )

    private fun <T> tieBreakOrder() = compareByDescending<RankedTrack<T>> { it.description.format == SubtitleTrackFormat.ASS_SSA }
        .thenBy { it.description.isForced }
        .thenBy { it.description.externalOrder ?: Int.MAX_VALUE }
        .thenBy { it.position }
}
