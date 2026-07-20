package com.mskd.flux.features

import com.mskd.flux.features.artwork.moduleArtwork
import com.mskd.flux.features.catalog.moduleCatalog
import com.mskd.flux.features.customization.moduleCustomization
import com.mskd.flux.features.images.moduleImages
import com.mskd.flux.features.progress.moduleProgress
import com.mskd.flux.features.search.moduleSearch
import com.mskd.flux.features.settings.moduleSettings
import com.mskd.flux.features.show.moduleShow
import com.mskd.flux.features.sources.moduleSources
import com.mskd.flux.features.token.moduleToken
import com.mskd.flux.features.unknown.moduleUnknown
import com.mskd.flux.features.welcome.moduleWelcome
import org.koin.dsl.module

val moduleFeatures = module {

    includes(
        moduleArtwork,
        moduleCatalog,
        moduleCustomization,
        moduleImages,
        moduleProgress,
        moduleSearch,
        moduleSettings,
        moduleShow,
        moduleSources,
        moduleToken,
        moduleUnknown,
        moduleWelcome
    )

}