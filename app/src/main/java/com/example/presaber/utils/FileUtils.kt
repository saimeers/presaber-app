package com.example.presaber.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun uriToFile(context: Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open uri")
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { output ->
        inputStream.copyTo(output)
    }
    inputStream.close()
    return file
}
