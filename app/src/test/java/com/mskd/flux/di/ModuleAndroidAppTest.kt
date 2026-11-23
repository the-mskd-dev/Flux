package com.mskd.flux.di

import android.content.Context
import com.mskd.flux.external.AndroidExternalPlayerAdapter
import com.mskd.flux.features.player.domain.playback.ExternalPlayerAdapter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class ModuleAndroidAppTest : FunSpec({

    afterTest { stopKoin() }

    // MainActivity injects the concrete adapter to attach its launcher; PlaybackLaunch injects the interface.
    test("the concrete External player adapter resolves to the instance seen through its interface") {
        val koin = startKoin {
            androidContext(mockk<Context>(relaxed = true))
            modules(moduleAndroidApp)
        }.koin

        koin.get<AndroidExternalPlayerAdapter>() shouldBeSameInstanceAs koin.get<ExternalPlayerAdapter>()
    }
})
