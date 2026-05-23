package com.ttsengine

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream

data class SaveResult(
    val displayPath: String,
    val uri: Uri?,
    val filePath: String?
)

object FileUtils {

    private const val APP_FOLDER = "TTS Engine"
    private const val MIME_MP4 = "audio/mp4"

    fun saveOutput(context: Context, sourceFile: File, fileName: String): SaveResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, sourceFile, fileName)
        } else {
            saveToLegacyPath(context, sourceFile, fileName)
        }
    }

    @android.annotation.TargetApi(Build.VERSION_CODES.Q)
    private fun saveViaMediaStore(context: Context, sourceFile: File, fileName: String): SaveResult {
        val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/$APP_FOLDER"

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, MIME_MP4)
            put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues
        ) ?: throw Exception("Gagal membuat file di $APP_FOLDER/")

        context.contentResolver.openOutputStream(uri)?.use { out ->
            FileInputStream(sourceFile).use { input -> input.copyTo(out) }
        } ?: throw Exception("Gagal menulis file")

        contentValues.clear()
        contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, contentValues, null, null)

        return SaveResult(
            displayPath = "Documents/$APP_FOLDER/$fileName",
            uri = uri,
            filePath = null
        )
    }

    private fun saveToLegacyPath(context: Context, sourceFile: File, fileName: String): SaveResult {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            APP_FOLDER
        )
        dir.mkdirs()

        val destFile = File(dir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)

        return SaveResult(
            displayPath = destFile.absolutePath,
            uri = null,
            filePath = destFile.absolutePath
        )
    }
}
