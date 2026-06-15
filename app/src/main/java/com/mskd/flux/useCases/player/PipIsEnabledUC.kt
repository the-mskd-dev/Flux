package com.mskd.flux.useCases.player

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager

class PipIsEnabledUC(
    private val context: Context
) {

    suspend operator fun invoke() : Boolean {

        val supportsPip = context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

        if (!supportsPip) {
            return false
        }

        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                android.os.Process.myUid(),
                context.packageName
            )

        return mode == AppOpsManager.MODE_ALLOWED
    }
}