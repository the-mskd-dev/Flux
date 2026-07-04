package com.mskd.flux.features

import com.mskd.flux.features.connectivity.moduleConnectivityAndroid
import com.mskd.flux.features.files.moduleFilesAndroid
import com.mskd.flux.features.images.moduleImagesAndroid
import com.mskd.flux.features.player.modulePlayerAndroid
import com.mskd.flux.features.sources.moduleSourcesAndroid
import org.koin.dsl.module

val moduleFeaturesAndroid = module {

    includes(
        moduleConnectivityAndroid,
        moduleFilesAndroid,
        moduleImagesAndroid,
        modulePlayerAndroid,
        moduleSourcesAndroid,
    )

}