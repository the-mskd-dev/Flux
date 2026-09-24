package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.report.CrashKey
import com.mskd.flux.report.CrashLogger
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.CrashDialogActivity
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import kotlin.time.Clock

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