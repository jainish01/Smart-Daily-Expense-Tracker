package com.example.smartdailyexpensetracker.utils

import android.net.Uri
import java.io.File
import java.io.InputStream

//saves image in local directory in app for future reference
fun saveImageToInternalStorage(context: android.content.Context, uri: Uri): String? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "receipt_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}