package com.mskd.flux.report

import com.mskd.flux.utils.Trace

class PlayCrashLogger : CrashLogger {

    override fun addCustomData(key: CrashKey, value: String) {
        Trace.debug("addCustomData: $key, $value", "PlayCrashLogger")
    }

    override fun addBreadcrumb(message: String) {
        Trace.debug( "addBreadcrumb: $message", "PlayCrashLogger")
    }

}