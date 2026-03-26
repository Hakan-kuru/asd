package com.hakankuru.agesa_insurtech_codenight.assest

import android.content.Context

fun getJsonFromAssets(context: Context, fileName: String): String? {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: Exception) {
        ioException.printStackTrace()
        null
    }
}