package com.mskd.flux.report

import android.app.Application
import com.mskd.flux.BuildConfig
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.CrashDialogActivity
import com.mskd.flux.utils.Trace
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import kotlin.time.Clock

class FossCrashLogger(private val app: Application) : CrashLogger {

    override fun init() {

        val timestamp = currentTimestampParis()
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        app.initAcra {
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

        addCustomData(key = CrashKey.FLAVOR, value = BuildConfig.FLAVOR)

    }

    override fun addCustomData(key: CrashKey, value: String) {
        if (!ACRA.isInitialised) return
        ACRA.errorReporter.putCustomData(key.key, value)
        Trace.debug("addCustomData: $key, $value", "FossCrashLogger")
    }

    override fun addBreadcrumb(message: String) {
        if (!ACRA.isInitialised) return
        ACRA.errorReporter.putCustomData(CrashKey.BREADCRUMB.key, message)
        Trace.debug( "addBreadcrumb: $message", "FossCrashLogger")
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