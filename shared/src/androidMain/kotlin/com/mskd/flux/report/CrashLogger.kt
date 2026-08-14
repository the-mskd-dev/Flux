package com.mskd.flux.report

import org.acra.ACRA


actual fun reportAddCustomData(key: String, value: String) {
    if (!ACRA.isInitialised) return
    ACRA.errorReporter.putCustomData(key, value)
}

actual fun reportAddBreadcrumb(message: String) {
    if (!ACRA.isInitialised) return
    ACRA.errorReporter.putCustomData("last_breadcrumb", message)
}