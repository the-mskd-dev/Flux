package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.core.StringProvider

enum class CatalogSorting(val description: StringProvider) {
    A_TO_Z(description = StringProvider.Static("A to Z")),
    Z_TO_A(description = StringProvider.Static("Z to A")),
    LAST_MODIFICATION(description = StringProvider.Static("Last modification")),
}