package com.mskd.flux.features

import com.mskd.flux.features.progress.moduleProgress
import com.mskd.flux.features.sources.moduleSources
import com.mskd.flux.features.tmdb.data.moduleTmdb
import org.koin.dsl.module

val moduleFeatures = module {

    includes(
        moduleSources,
        moduleTmdb,
        moduleProgress
    )

}