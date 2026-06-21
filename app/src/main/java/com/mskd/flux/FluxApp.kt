package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.globalModule
import com.mskd.flux.di.imageModule
import com.mskd.flux.di.moduleDatabaseAndroid
import com.mskd.flux.di.moduleDatastoreAndroid
import com.mskd.flux.di.moduleRepositoryAndroid
import com.mskd.flux.di.playerModule
import com.mskd.flux.di.viewModelsModule
import com.mskd.flux.di.moduleCoroutine
import com.mskd.flux.di.moduleDatabase
import com.mskd.flux.di.moduleDatastore
import com.mskd.flux.di.moduleNetwork
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.di.moduleRepository
import com.mskd.flux.di.moduleUseCase
import com.mskd.flux.di.moduleUseCaseAndroid
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

            modules(

                modulePlatform,

                moduleCoroutine,

                moduleDatabase,
                moduleDatabaseAndroid,

                moduleDatastore,
                moduleDatastoreAndroid,

                moduleNetwork,

                moduleRepository,
                moduleRepositoryAndroid,

                moduleUseCase,
                moduleUseCaseAndroid,

                globalModule,
                imageModule,
                playerModule,
                viewModelsModule
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