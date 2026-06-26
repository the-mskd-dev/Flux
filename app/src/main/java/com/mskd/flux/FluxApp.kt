package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.Properties
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.CrashDialogActivity
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FluxApp : Application(), SingletonImageLoader.Factory {
    val imageLoader: ImageLoader by inject()

    override fun newImageLoader(context: Context): ImageLoader = imageLoader

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        startKoin {
            androidContext(this@FluxApp)

            properties(
                mapOf(
                    Properties.IS_DEBUG to BuildConfig.DEBUG,
                    Properties.VERSION_NAME to BuildConfig.VERSION_NAME,
                    Properties.VERSION_CODE to BuildConfig.VERSION_CODE,
                    Properties.DEBUG_TOKEN to BuildConfig.TMDB_TOKEN,
                )
            )

            modules(
                modulePlatform,
                moduleAndroidApp
            )

        }

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST

            mailSender {
                mailTo = Constants.CONTACT.MAIL
                subject = "Flux - Crash Report"
            }

            dialog {
                reportDialogClass = CrashDialogActivity::class.java
            }

        }

    }

}