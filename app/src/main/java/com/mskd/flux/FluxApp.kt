package com.mskd.flux

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
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

    override fun newImageLoader(context: Context): ImageLoader = imageLoader

    override fun onCreate() {
        super.onCreate()

        val timestamp = currentTimestampParis()
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST

            reportContent = listOf(
                ReportField.REPORT_ID,
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.USER_CRASH_DATE,
            )

            mailSender {
                mailTo = Constants.CONTACT.MAIL
                subject = "Flux - Crash Report - $versionName - $versionCode"
                reportFileName = "Crash Report - $versionCode - $timestamp.txt"
            }

            dialog {
                reportDialogClass = CrashDialogActivity::class.java
            }

        }

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        startKoin {
            androidContext(this@FluxApp)

            modules(
                modulePlatform,
                moduleAndroidApp
            )

        }

    }

}

private fun currentTimestampParis(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.of("Europe/Paris"))
    return buildString {
        append(now.day.toString().padStart(2, '0'))
        append('-')
        append(now.month.number.toString().padStart(2, '0'))
        append('_')
        append(now.hour.toString().padStart(2, '0'))
        append('h')
        append(now.minute.toString().padStart(2, '0'))
    }
}