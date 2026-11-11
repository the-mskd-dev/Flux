package com.mskd.flux.navigation.domain

class FluxNavigator(
    val navigate: (Route) -> Unit,
    val onBack: () -> Unit,
    val clearAndNavigate: (Route) -> Unit,
    /** Replaces the current destination: back leads to the one before it. */
    val replace: (Route) -> Unit,
    /** Replaces the current page and the pages of the same Artwork right below it: see [replacePagesOf]. */
    val replacePagesOf: (artworkId: Long, Route) -> Unit,
)