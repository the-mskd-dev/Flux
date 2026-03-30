package com.mskd.flux.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object WebLink {

    fun openPage(context: Context, url: String) {
        val webpage = url.toUri()
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        context.startActivity(intent)
    }

}