package com.mskd.flux.features.people

import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.people.data.dao.PersonDao
import com.mskd.flux.features.people.data.repository.PeopleRepositoryImpl
import com.mskd.flux.features.people.domain.repository.PeopleRepository
import com.mskd.flux.features.people.domain.usecase.GetPeopleUseCase
import com.mskd.flux.features.people.domain.usecase.GetPersonDetailsUseCase
import com.mskd.flux.features.people.domain.usecase.SyncPeopleUseCase
import com.mskd.flux.features.people.presentation.PersonViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val modulePeople = module {

    single<PersonDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.personDao()
    }

    single<PeopleRepository> {
        PeopleRepositoryImpl(
            dao = get()
        )
    }

    singleOf(::GetPeopleUseCase)

    singleOf(::GetPersonDetailsUseCase)

    single {
        SyncPeopleUseCase(
            databaseRepository = get(),
            apiRepository = get(),
            peopleRepository = get(),
            userDataStore = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE),
        )
    }

    viewModel { params ->
        PersonViewModel(
            person = params.get(),
            getPersonDetailsUseCase = get(),
        )
    }

}