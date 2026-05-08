package com.mskd.flux.useCases.catalogUC

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.model.Catalog
import javax.inject.Inject

interface CatalogUC {

    suspend fun syncCatalog() : Catalog

}

class CatalogUCImpl @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val databaseRepository: DatabaseRepository,
    private val filesRepository: FilesRepository
) : CatalogUC {

    override suspend fun syncCatalog(): Catalog {
        TODO("Not yet implemented")
    }

}