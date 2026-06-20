package com.mskd.flux.shared.di

import com.mskd.flux.shared.data.repository.token.TokenRepository
import com.mskd.flux.shared.data.repository.token.TokenRepositoryImpl
import org.koin.dsl.module

val moduleDatastore = module {

    single<TokenRepository> {
        TokenRepositoryImpl(
            tokenDataStore = get(Qualifiers.TOKEN_DATASTORE)
        )
    }

}