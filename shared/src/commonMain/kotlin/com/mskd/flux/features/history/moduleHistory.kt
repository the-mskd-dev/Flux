package com.mskd.flux.features.history

import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.features.history.data.dao.HistoryDao
import com.mskd.flux.features.history.data.repository.HistoryRepositoryImpl
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.history.domain.usecase.SaveToHistoryUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleHistory = module {

    single<HistoryDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.historyDao()
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(
            dao = get()
        )
    }

    singleOf(::SaveToHistoryUseCase)

}