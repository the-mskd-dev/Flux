package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.coroutineModule
import com.mskd.flux.di.globalModule
import com.mskd.flux.di.imageModule
import com.mskd.flux.di.moduleDatabaseAndroid
import com.mskd.flux.di.moduleDatastoreAndroid
import com.mskd.flux.di.moduleRepositoryAndroid
import com.mskd.flux.di.playerModule
import com.mskd.flux.di.useCasesModule
import com.mskd.flux.di.viewModelsModule
import com.mskd.flux.shared.di.moduleDatabase
import com.mskd.flux.shared.di.moduleDatastore
import com.mskd.flux.shared.di.moduleNetwork
import com.mskd.flux.shared.di.moduleRepository
import com.mskd.flux.shared.utils.Constants
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
                coroutineModule,

                moduleDatabase,
                moduleDatabaseAndroid,

                moduleDatastore,
                moduleDatastoreAndroid,

                moduleNetwork,

                moduleRepository,
                moduleRepositoryAndroid,

                globalModule,
                imageModule,
                playerModule,
                useCasesModule,
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