package com.mskd.flux.features.credits

import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.features.credits.data.dao.PeopleDao
import com.mskd.flux.features.credits.data.repository.CreditsRepositoryImpl
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import com.mskd.flux.features.credits.domain.usecase.GetCreditsUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleCredits = module {

    single<PeopleDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.peopleDao()
    }

    single<CreditsRepository> {
        CreditsRepositoryImpl(
            dao = get()
        )
    }

    singleOf(::GetCreditsUseCase)

}