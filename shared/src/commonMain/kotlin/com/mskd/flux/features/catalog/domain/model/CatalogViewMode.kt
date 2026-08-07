package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.core.StringProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.by_genre
import flux.shared.generated.resources.by_type
import flux.shared.generated.resources.grid
import flux.shared.generated.resources.ic_grid
import flux.shared.generated.resources.ic_list
import org.jetbrains.compose.resources.DrawableResource

enum class CatalogViewMode(val drawableRes: DrawableResource, val description: StringProvider) {
    GRID(drawableRes = Res.drawable.ic_grid, description = StringProvider.Resource(Res.string.grid)),
    BY_TYPE(drawableRes = Res.drawable.ic_list, description = StringProvider.Resource(Res.string.by_type)),
    BY_GENRE(drawableRes = Res.drawable.ic_list, description = StringProvider.Resource(Res.string.by_genre));

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