package com.mskd.flux.core

import com.mskd.flux.core.database.moduleDatabaseAndroid
import com.mskd.flux.core.datastore.moduleDatastoreAndroid
import org.koin.dsl.module

val moduleCoreAndroid = module {

    includes(
        moduleDatabaseAndroid,
        moduleDatastoreAndroid,
    )

}