package com.mskd.flux.features.identification

import com.mskd.flux.features.catalog.domain.sync.enrichment.DetailsEnrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.DurationsEnrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.SeasonsEnrichment
import com.mskd.flux.features.identification.domain.usecase.IdentifyManuallyUseCase
import com.mskd.flux.features.identification.presentation.IdentificationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleIdentification = module {

    // The Enrichissements of the catalogue step, without the identification: the Artwork is the one chosen by the user
    single {
        IdentifyManuallyUseCase(
            database = get(),
            enrichments = listOf(
                DetailsEnrichment(api = get()),
                SeasonsEnrichment(api = get(), missingSeasonIsUnknown = true),
                DurationsEnrichment(getFileDurationUseCase = get()),
            ),
        )
    }

    viewModel { params ->
        IdentificationViewModel(
            artworkId = params.get(),
            databaseRepository = get(),
            apiRepository = get(),
            identifyManually = get(),
        )
    }

}
