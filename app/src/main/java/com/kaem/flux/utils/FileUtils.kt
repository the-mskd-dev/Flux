package com.kaem.flux.utils

fun parseFileName(fileName: String) : String {

    val splitFileName = fileName.split("_")

    when {

        splitFileName.size == 1 -> return splitFileName[0]
        splitFileName.size == 2 -> return splitFileName[1]

        else -> return ""
    }

}