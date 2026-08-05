package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode.A_TO_Z
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode.LAST_MODIFICATION
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode.Z_TO_A
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_sort
import org.jetbrains.compose.resources.DrawableResource

enum class CatalogViewMode(val drawableRes: DrawableResource, val description: StringProvider) {
    BY_TYPE(drawableRes = Res.drawable.ic_sort, description = StringProvider.Static("By type")),
    BY_GENRE(drawableRes = Res.drawable.ic_sort, description = StringProvider.Static("By genre")),
    GRID(drawableRes = Res.drawable.ic_sort, description = StringProvider.Static("Grid"));

    companion object {

        fun fromOrdinal(ordinal: Int) : CatalogViewMode {
            return when (ordinal) {
                GRID.ordinal -> GRID
                BY_GENRE.ordinal -> BY_GENRE
                else -> BY_TYPE
            }
        }

    }
}