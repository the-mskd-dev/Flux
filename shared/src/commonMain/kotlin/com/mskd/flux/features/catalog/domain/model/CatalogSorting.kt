package com.mskd.flux.features.catalog.domain.model

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.StringProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.a_to_z
import flux.shared.generated.resources.last_modification
import flux.shared.generated.resources.z_to_a

@Immutable
data class CatalogSorting(
    val option: CatalogSortingOption = CatalogSortingOption.LAST_MODIFICATION,
    val showOptions: Boolean = false
)

enum class CatalogSortingOption(val description: StringProvider) {
    LAST_MODIFICATION(description = StringProvider.Resource(Res.string.last_modification)),
    A_TO_Z(description = StringProvider.Resource(Res.string.a_to_z)),
    Z_TO_A(description = StringProvider.Resource(Res.string.z_to_a));

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