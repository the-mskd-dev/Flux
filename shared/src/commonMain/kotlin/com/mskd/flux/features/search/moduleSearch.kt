package com.mskd.flux.features.search

import com.mskd.flux.features.search.presentation.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleSearch = module {

    viewModel { params ->
        SearchViewModel(
            contentType = params.getOrNull(),
            database = get(),
            settingsDataStore = get()
        )
    }

}