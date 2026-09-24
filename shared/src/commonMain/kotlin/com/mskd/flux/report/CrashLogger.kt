package com.mskd.flux.report

interface CrashLogger {
    fun init()
    fun addCustomData(key: CrashKey, value: String)
    fun addBreadcrumb(message: String)
}