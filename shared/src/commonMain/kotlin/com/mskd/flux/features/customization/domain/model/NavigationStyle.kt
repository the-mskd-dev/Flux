package com.mskd.flux.features.customization.domain.model

import com.mskd.flux.core.model.core.StringProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.bottom_bar
import flux.shared.generated.resources.pill
import flux.shared.generated.resources.top_bar

enum class NavigationStyle(val description: StringProvider) {
    PILL(description = StringProvider.Resource(Res.string.pill)),
    TOP_BAR(description = StringProvider.Resource(Res.string.top_bar)),
    BOTTOM_BAR(description = StringProvider.Resource(Res.string.bottom_bar)),;

    companion object {

        fun fromOrdinal(ordinal: Int) : NavigationStyle = when (ordinal) {
            1 -> TOP_BAR
            2 -> BOTTOM_BAR
            else -> PILL
        }
    }
}