package com.mskd.flux.features.customization.domain.model

import com.mskd.flux.ui.text.TextProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.bottom_bar
import flux.shared.generated.resources.pill
import flux.shared.generated.resources.top_bar

enum class NavigationStyle(val description: TextProvider) {
    PILL(description = TextProvider.Resource(Res.string.pill)),
    TOP_BAR(description = TextProvider.Resource(Res.string.top_bar)),
    BOTTOM_BAR(description = TextProvider.Resource(Res.string.bottom_bar)),;

    companion object {

        fun fromOrdinal(ordinal: Int) : NavigationStyle = when (ordinal) {
            1 -> TOP_BAR
            2 -> BOTTOM_BAR
            else -> PILL
        }
    }
}