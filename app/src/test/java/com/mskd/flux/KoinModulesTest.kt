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
import io.kotest.core.spec.style.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verifyAll


class KoinModulesTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyModules() {

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

        allModules.verifyAll(
            extraTypes = listOf(
                Context::class,
                Application::class,
                String::class,
                Boolean::class,
                Int::class,
                ContentType::class
            )
        )

    }
}