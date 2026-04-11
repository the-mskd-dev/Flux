package com.mskd.flux.screens.unknown

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.catalog.CatalogRepository
import com.mskd.flux.mockups.FakeArtworkRepository
import com.mskd.flux.mockups.FakeCatalogRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.screens.search.SearchViewModel
import io.kotest.core.spec.style.FunSpec

class UnknownViewModelTest : FunSpec ({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var artworkRepository: FakeArtworkRepository

    beforeTest {

        artworkRepository = FakeArtworkRepository(unknown = true)

        viewModel = UnknownViewModel(repository = artworkRepository)

    }

})