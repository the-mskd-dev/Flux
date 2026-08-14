package com.mskd.flux.report

import org.acra.ACRA

actual fun reportAddCustomData(key: CrashKey, value: String) {
    if (!ACRA.isInitialised) return
    ACRA.errorReporter.putCustomData(key.key, value)
}

actual fun reportAddBreadcrumb(message: String) {
    if (!ACRA.isInitialised) return
    ACRA.errorReporter.putCustomData(CrashKey.BREADCRUMB.key, message)
}