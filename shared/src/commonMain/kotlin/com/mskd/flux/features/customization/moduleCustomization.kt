package com.mskd.flux.features.customization

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.customization.data.datastore.CustomizationDataStoreImpl
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.customization.presentation.CustomizationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleCustomization = module {

    single<CustomizationDataStore> {
        CustomizationDataStoreImpl(
            customizationDataStore = get(Qualifiers.CUSTOMIZATION_DATASTORE)
        )
    }

    viewModelOf(::CustomizationViewModel)

}