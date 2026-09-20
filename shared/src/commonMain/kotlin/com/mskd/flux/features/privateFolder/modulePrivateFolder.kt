package com.mskd.flux.features.privateFolder

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.privateFolder.data.datastore.PrivateFolderDataStoreImpl
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.disablePrivateFolder.DisablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder.EnablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.observePrivateFolder.ObservePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.observePrivateFolder.ObservePrivateFolderUseCaseImpl
import com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy.SetArtworkPrivacyUseCase
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val modulePrivateFolder = module {

    viewModelOf(::PrivateFolderViewModel)

    single<PrivateFolderDataStore> {
        PrivateFolderDataStoreImpl(
            privateFolderDataStore = get(Qualifiers.PRIVATE_FOLDER_DATASTORE)
        )
    }

    single<ObservePrivateFolderUseCase> {
        ObservePrivateFolderUseCaseImpl(
            privateFolderDataStore = get()
        )
    }

    singleOf(::EnablePrivateFolderUseCase)
    singleOf(::DisablePrivateFolderUseCase)
    singleOf(::SetArtworkPrivacyUseCase)

}
