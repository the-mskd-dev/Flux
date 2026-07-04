package com.mskd.flux.di

import com.mskd.flux.utils.interceptors.NetworkImageInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleImages = module {

    singleOf(::NetworkImageInterceptor)

}