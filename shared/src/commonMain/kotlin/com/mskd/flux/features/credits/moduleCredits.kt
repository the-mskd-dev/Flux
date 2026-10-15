package com.mskd.flux.features.credits

import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.features.credits.data.dao.PersonDao
import com.mskd.flux.features.credits.data.repository.CreditsRepositoryImpl
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import com.mskd.flux.features.credits.domain.usecase.GetCreditsUseCase
import com.mskd.flux.features.credits.domain.usecase.GetPersonDetailsUseCase
import com.mskd.flux.features.credits.presentation.PersonViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleCredits = module {

    single<PersonDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.personDao()
    }

    single<CreditsRepository> {
        CreditsRepositoryImpl(
            dao = get()
        )
    }

    singleOf(::GetCreditsUseCase)

    singleOf(::GetPersonDetailsUseCase)

    viewModel { params ->
        PersonViewModel(
            person = params.get(),
            getPersonDetailsUseCase = get(),
        )
    }

}