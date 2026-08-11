package com.mskd.flux.features.search

import com.mskd.flux.features.search.presentation.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleSearch = module {

    viewModel { params ->
        SearchViewModel(
            withType = params.getOrNull(),
            withGenre = params.getOrNull(),
            database = get(),
            details = get(),
            settings = get(),
        )
    }

}