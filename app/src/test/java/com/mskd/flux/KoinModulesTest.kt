package com.mskd.flux

import android.app.Application
import android.content.Context
import com.mskd.flux.di.ModuleUseCases
import com.mskd.flux.di.coroutineModule
import com.mskd.flux.di.globalModule
import com.mskd.flux.di.imageModule
import com.mskd.flux.di.moduleDatabaseAndroid
import com.mskd.flux.di.moduleDatastore
import com.mskd.flux.di.moduleDatastoreAndroid
import com.mskd.flux.di.moduleNetwork
import com.mskd.flux.di.modulePlayer
import com.mskd.flux.di.moduleRepository
import com.mskd.flux.di.moduleRepositoryAndroid
import com.mskd.flux.di.moduleViewModels
import com.mskd.flux.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify


class KoinModulesTest : FunSpec({

    @OptIn(KoinExperimentalAPI::class)
    test("verify modules") {

        val allModules = listOf(
            coroutineModule,
            moduleDatabaseAndroid,
            moduleDatastore,
            moduleDatastoreAndroid,
            moduleNetwork,
            moduleRepository,
            moduleRepositoryAndroid,
            globalModule,
            imageModule,
            modulePlayer,
            ModuleUseCases,
            moduleViewModels
        )

        val combined = module {
            includes(allModules)
        }

        combined.verify(
            extraTypes = listOf(
                Context::class,
                Application::class,
                String::class,
                Boolean::class,
                Int::class,
                ContentType::class,
                io.ktor.client.engine.HttpClientEngine::class
            )
        )

    }
})