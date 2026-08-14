package com.mskd.flux.report

expect fun reportAddCustomData(key: CrashKey, value: String)
expect fun reportAddBreadcrumb(message: String)
