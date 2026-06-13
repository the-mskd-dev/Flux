package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.coroutineModule
import com.mskd.flux.di.dataStoreModule
import com.mskd.flux.di.databaseModule
import com.mskd.flux.di.globalModule
import com.mskd.flux.di.imageModule
import com.mskd.flux.di.ktorModule
import com.mskd.flux.di.playerModule
import com.mskd.flux.di.repositoriesModule
import com.mskd.flux.di.useCasesModule
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.CrashDialogActivity
import dagger.hilt.android.HiltAndroidApp
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import javax.inject.Inject

@HiltAndroidApp
class FluxApp : Application(), SingletonImageLoader.Factory {
    @Inject lateinit var imageLoader: ImageLoader

    override fun newImageLoader(context: Context): ImageLoader = imageLoader

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        startKoin {
            androidContext(this@FluxApp)

            modules(
                coroutineModule,
                databaseModule,
                dataStoreModule,
                globalModule,
                imageModule,
                ktorModule,
                playerModule,
                repositoriesModule,
                useCasesModule
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