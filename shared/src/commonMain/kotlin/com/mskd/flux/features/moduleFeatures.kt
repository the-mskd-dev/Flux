package com.mskd.flux.features

import com.mskd.flux.features.sources.moduleSources
import org.koin.dsl.module
import org.koin.plugin.module.dsl.includes

val moduleFeatures = module {

    includes(
        moduleSources
    )

}