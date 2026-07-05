package com.mskd.flux.features

import com.mskd.flux.features.catalog.moduleCatalog
import com.mskd.flux.features.images.moduleImages
import com.mskd.flux.features.progress.moduleProgress
import com.mskd.flux.features.sources.moduleSources
import com.mskd.flux.features.tmdb.data.moduleTmdb
import org.koin.dsl.module

val moduleFeatures = module {

    includes(
        moduleCatalog,
        moduleImages,
        moduleProgress,
        moduleSources,
        moduleTmdb,
    )

}