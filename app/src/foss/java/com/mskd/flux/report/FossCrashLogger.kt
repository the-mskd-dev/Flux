package com.mskd.flux.report

import com.mskd.flux.utils.Trace
import org.acra.ACRA

class FossCrashLogger : CrashLogger {

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