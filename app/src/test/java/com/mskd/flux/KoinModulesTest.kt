package com.mskd.flux

import android.app.Application
import android.content.Context
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.features.player.presentation.PlayerViewModel
import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
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
                AppInfo::class,
                io.ktor.client.engine.HttpClientEngine::class
            ),
            injections = injectedParameters(
                definition<PlayerViewModel<*>>(PlayerParams::class)
            )
        )

    }
})