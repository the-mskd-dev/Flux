package com.mskd.flux.features.catalog.domain.model

/** Kind of Sync run by the Sync pipeline. */
enum class SyncKind {
    /** Processes only new files and removes Missing files. */
    LIGHT,
    /** Fetches TMDB data for every file again, updates Genres and resets user choices. */
    FULL,
    /** Updates the Catalog in the selected data language without scanning files. */
    LANGUAGE_CHANGE,
}
