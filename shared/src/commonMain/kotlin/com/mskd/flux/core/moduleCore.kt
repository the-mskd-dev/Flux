package com.mskd.flux.core

import com.mskd.flux.core.data.database.moduleDatabase
import com.mskd.flux.core.domain.datastore.moduleDatastore
import org.koin.dsl.module

val moduleCore = module {

    includes(
        moduleDatastore,
        moduleDatabase,
    )

}