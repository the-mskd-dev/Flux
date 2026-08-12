package com.mskd.flux.features.customization.domain.model

enum class NavigationStyle {
    PILL, TOP_BAR, BOTTOM_BAR;

    companion object {

        fun fromOrdinal(ordinal: Int) : NavigationStyle = when (ordinal) {
            1 -> TOP_BAR
            2 -> BOTTOM_BAR
            else -> PILL
        }
    }
}