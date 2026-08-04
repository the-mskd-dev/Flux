package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.core.StringProvider

data class CatalogSorting(
    val option: CatalogSortingOption = CatalogSortingOption.LAST_MODIFICATION,
    val showOptions: Boolean = false
)

enum class CatalogSortingOption(val description: StringProvider) {
    LAST_MODIFICATION(description = StringProvider.Static("Last modification")),
    A_TO_Z(description = StringProvider.Static("A to Z")),
    Z_TO_A(description = StringProvider.Static("Z to A"));

    companion object {

        fun fromOrdinal(ordinal: Int) : CatalogSortingOption {
            return when (ordinal) {
                A_TO_Z.ordinal -> A_TO_Z
                Z_TO_A.ordinal -> Z_TO_A
                else -> LAST_MODIFICATION
            }
        }

    }

}