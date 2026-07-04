package com.mskd.flux.core

import com.mskd.flux.core.datastore.moduleDatastore
import com.mskd.flux.core.database.moduleDatabase
import com.mskd.flux.core.util.moduleUtil
import org.koin.dsl.module

val moduleCore = module {

    includes(
        moduleDatastore,
        moduleDatabase,
        moduleUtil
    )

}