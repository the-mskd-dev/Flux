package com.mskd.flux.report

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mskd.flux.BuildConfig

class PlayCrashLogger : CrashLogger {

    override fun init() {
        addCustomData(key = CrashKey.FLAVOR, value = BuildConfig.FLAVOR)
    }

    override fun addCustomData(key: CrashKey, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key.key, value)
    }

    override fun addBreadcrumb(message: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(CrashKey.BREADCRUMB.key, message)
    }

}