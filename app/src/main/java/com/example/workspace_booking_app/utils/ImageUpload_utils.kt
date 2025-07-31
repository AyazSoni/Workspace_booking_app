package com.example.workspace_booking_app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun handleImageSelection(context: Context, uri: Uri, folder: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
            }

            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            return saveBitmapToInternalStorage(context, bitmap, folder)
        } else {
            throw RuntimeException("Unsupported OS Version")
        }
    }

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, folder: String): String {
        val dir = File(context.filesDir, folder)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "banner.png")
        if (file.exists()) file.delete()

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        return file.absolutePath
    }

    fun setImageFromPath(imageView: ImageView?, path: String) {
        val file = File(path)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView?.setImageBitmap(bitmap)
        }
    }
}
