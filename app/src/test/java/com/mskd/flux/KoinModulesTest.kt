package com.mskd.flux

import android.app.Application
import android.content.Context
import com.mskd.flux.di.coroutineModule
import com.mskd.flux.di.dataStoreModule
import com.mskd.flux.di.databaseModule
import com.mskd.flux.di.globalModule
import com.mskd.flux.di.imageModule
import com.mskd.flux.di.ktorModule
import com.mskd.flux.di.playerModule
import com.mskd.flux.di.repositoriesModule
import com.mskd.flux.di.useCasesModule
import com.mskd.flux.di.viewModelsModule
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
            databaseModule,
            dataStoreModule,
            globalModule,
            imageModule,
            ktorModule,
            playerModule,
            repositoriesModule,
            useCasesModule,
            viewModelsModule
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