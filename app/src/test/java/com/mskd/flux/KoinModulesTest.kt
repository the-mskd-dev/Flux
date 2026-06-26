package com.mskd.flux

import android.app.Application
import android.content.Context
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify


class KoinModulesTest : FunSpec({

    @OptIn(KoinExperimentalAPI::class)
    test("verify modules") {

        val allModules = module {
            includes(moduleAndroidApp, modulePlatform)
        }

        allModules.verify(
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