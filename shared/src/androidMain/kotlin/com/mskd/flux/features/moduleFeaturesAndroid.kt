package com.mskd.flux.features

import com.mskd.flux.features.sources.moduleSourcesAndroid
import org.koin.dsl.module

val moduleFeaturesAndroid = module {

    includes(
        moduleSourcesAndroid
    )

}