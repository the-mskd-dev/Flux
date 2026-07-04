package com.mskd.flux.core

import com.mskd.flux.core.datastore.moduleDatastore
import com.mskd.flux.core.database.moduleDatabase
import com.mskd.flux.features.images.moduleImages
import org.koin.dsl.module

val moduleCore = module {

    includes(
        moduleDatastore,
        moduleDatabase,
    )

}