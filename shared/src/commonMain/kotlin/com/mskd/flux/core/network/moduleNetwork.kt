package com.mskd.flux.core.network

import com.mskd.flux.core.network.tmdb.data.moduleTmdb
import org.koin.dsl.module

val moduleNetwork  = module {

    includes(moduleTmdb)

}