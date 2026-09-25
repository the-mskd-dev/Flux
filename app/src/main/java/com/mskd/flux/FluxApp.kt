package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.report.CrashLogger
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named

class FluxApp : Application(), SingletonImageLoader.Factory {
    val imageLoader: ImageLoader by inject(qualifier = named("uiImageLoader"))
    val crashLogger: CrashLogger by inject()

    override fun newImageLoader(context: Context): ImageLoader = imageLoader

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        startKoin {
            androidContext(this@FluxApp)

            modules(
                modulePlatform,
                moduleAndroidApp,
                moduleFlavor
            )

        }

        crashLogger.init()

    }

}