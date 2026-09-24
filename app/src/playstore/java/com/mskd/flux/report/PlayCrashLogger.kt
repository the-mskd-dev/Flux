package com.mskd.flux.report

import com.mskd.flux.BuildConfig
import com.mskd.flux.utils.Trace

class PlayCrashLogger : CrashLogger {

    override fun init() {
        Trace.debug("init", "PlayCrashLogger")
        addCustomData(key = CrashKey.FLAVOR, value = BuildConfig.FLAVOR)
    }

    override fun addCustomData(key: CrashKey, value: String) {
        Trace.debug("addCustomData: $key, $value", "PlayCrashLogger")
    }

    override fun addBreadcrumb(message: String) {
        Trace.debug( "addBreadcrumb: $message", "PlayCrashLogger")
    }

}