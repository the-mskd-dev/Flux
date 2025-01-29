package com.kaem.flux.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object WebLink {

    fun openPage(context: Context, url: String) {
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        context.startActivity(intent)
    }

}